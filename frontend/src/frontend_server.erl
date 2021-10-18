-module(frontend_server).

-export([start_server/1, districtMap/0]).

start_server(Port) ->
    {ok, LSock} = gen_tcp:listen(Port, [
        binary,
        {active, once},
        {packet, line},
        {reuseaddr, true}
    ]),
    district_manager:startDistrictManager(),
    Distritos = districtMap(),
    [roomM ! {create, Porta} || {_, Porta} <- maps:to_list(Distritos)],
    spawn(fun() -> startLoginManager() end),
    spawn(fun() -> acceptor(LSock, Distritos) end),
    ok.

districtMap() ->
    Config = os:getenv("APP_CONFIG_FILE", "config/prod_config.erl"),
    {ok, Result} = file:consult(Config),
    maps:from_list(Result).

acceptor(LSock, Distritos) ->
    {ok, Sock} = gen_tcp:accept(LSock),
    spawn(fun() -> acceptor(LSock, Distritos) end),
    preUser(Sock, Distritos).

rpc(Req) ->
    loginManager ! Req,
    receive
        {Res, loginManager} -> Res
    end,
    Res.

receiveWrapper(Sock) ->
    receive
        {ok, Data} ->
            gen_tcp:send(Sock, Data),
            DistrictPid = 0;
        {ok_nots, DistrictPid} ->
            io:format("district pid received~n", []);
        {ok_join, DistrictPid} ->
            gen_tcp:send(Sock, <<"Logged In!\n">>)
    end,
    DistrictPid.

preUser(Sock, Distritos) ->
    Self = self(),
    receive
        {tcp, _, Data} ->
            case Data of
                <<"create ", Rest/binary>> ->
                    %TODO Be able to tell and protect the server when the arguments are wrong
                    [User, Password, District] = string:split(Rest, " ", all),
                    Res = rpc({create_account, User, Password, District, Self}),
                    case Res of
                        ok ->
                            inet:setopts(Sock, [{active, once}]),
                            case maps:find(District, Distritos) of
                                {ok, Porta} ->
                                    roomM ! {addUser, Porta, User, Self},
                                    receiveWrapper(Sock)
                            end;
                        _ ->
                            inet:setopts(Sock, [{active, once}]),
                            gen_tcp:send(Sock, <<"couldn't create account!\n">>)
                    end,
                    preUser(Sock, Distritos);
                <<"login ", Rest/binary>> ->
                    [User, Password, District] = string:split(Rest, " ", all),
                    Res = rpc({login, User, Password, District, Self}),
                    case Res of
                        ok ->
                            inet:setopts(Sock, [{active, once}]),
                            case maps:find(District, Distritos) of
                                {ok, Porta} ->
                                    roomM ! {join, Porta, Self},
                                    io:format("found district!~n", []),
                                    DistrictPid = receiveWrapper(Sock),
                                    receiveWrapper(Sock),
                                    user(User, Sock, DistrictPid, Distritos);
                                _ ->
                                    preUser(Sock, Distritos)
                            end;
                        _ ->
                            inet:setopts(Sock, [{active, once}]),
                            gen_tcp:send(Sock, <<"couldn't login account!\n">>),
                            preUser(Sock, Distritos)
                    end;
                _ ->
                    inet:setopts(Sock, [{active, once}]),
                    gen_tcp:send(Sock, <<"command not recognized!\n">>),
                    preUser(Sock, Distritos)
            end
    end.

receiveAnswer(User, Sock, DistrictPid, Distritos) ->
    receive
        {ok, Data} -> gen_tcp:send(Sock, Data)
    end,
    user(User, Sock, DistrictPid, Distritos).

user(User, Sock, DistrictPid, Distritos) ->
    Self = self(),
    io:format("i'm an user~n", []),
    receive
        {tcp, Info, Data} ->
            inet:setopts(Sock, [{active, once}]),
            io:format("Info is ~p~n", [Info]),
            case Data of
                <<"change_location ", Rest/binary>> ->
                    io:format("sent to district~n", []),
                    DistrictPid ! {location, User, Rest, Self},
                    receiveAnswer(User, Sock, DistrictPid, Distritos);
                <<"get_people ", Rest/binary>> ->
                    io:format("sent to district~n", []),
                    DistrictPid ! {number, Rest, Self},
                    receiveAnswer(User, Sock, DistrictPid, Distritos);
                <<"get_nots ", Distrito/binary>> ->
                    case maps:find(Distrito, Distritos) of
                        {ok, Porta} ->
                            roomM ! {contact, Porta, Self},
                            io:format("found district!~n", []),
                            NotsPid = receiveWrapper(Sock),
                            io:format("received ~p~n", [NotsPid]),
                            NotsPid ! {notifications, Self},
                            receiveAnswer(User, Sock, DistrictPid, Distritos);
                        _ ->
                            user(User, Sock, DistrictPid, Distritos)
                    end;
                <<"sick", _/binary>> ->
                    DistrictPid ! {sick, User, Self},
                    receiveAnswer(User, Sock, DistrictPid, Distritos);
                _ ->
                    gen_tcp:send(Sock, <<"Invalid command!\n">>),
                    user(User, Sock, DistrictPid, Distritos)
            end
    end.

startLoginManager() ->
    LoginManagerPid = spawn(fun() -> loop(#{}) end),
    register(loginManager, LoginManagerPid).

loop(Accounts) ->
    receive
        {create_account, User, Password, District, From} ->
            case maps:find(User, Accounts) of
                {ok, _} ->
                    From ! {user_exists, loginManager},
                    loop(Accounts);
                error ->
                    From ! {ok, loginManager},
                    loop(maps:put(User, {Password, false, District}, Accounts))
            end;
        {close_account, User, Password, From} ->
            case maps:find(User, Accounts) of
                {ok, {Password, _}} ->
                    From ! {ok, loginManager},
                    loop(maps:remove(User, Accounts));
                _ ->
                    From ! {invalid, loginManager},
                    loop(Accounts)
            end;
        {login, User, Password, District, From} ->
            case maps:find(User, Accounts) of
                {ok, {Password, false, District}} ->
                    From ! {ok, loginManager},
                    loop(maps:update(User, {Password, true, District}, Accounts));
                _ ->
                    From ! {invalid, loginManager},
                    loop(Accounts)
            end;
        {logout, User, Password, District, From} ->
            case maps:find(User, Accounts) of
                {ok, {Password, true}} ->
                    From ! {ok, loginManager},
                    loop(maps:update(User, {Password, false, District}, Accounts));
                _ ->
                    From ! {invalid, loginManager},
                    loop(Accounts)
            end;
        {online, From} ->
            From ! {maps:keys(maps:filter(fun(_, {_, true}) -> true end, Accounts)), loginManager},
            loop(Accounts)
    end.
