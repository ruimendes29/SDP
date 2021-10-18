-module(district_manager).

-export([startDistrictManager/0]).

startDistrictManager() ->
    RoomM = spawn(fun() -> roomManager(#{}) end),
    register(roomM, RoomM),
    RoomM,
    ok.

roomManager(Rooms) ->
    receive
        {create, DistrictPort} ->
            {ok, DistrictSocket} = gen_tcp:connect("localhost", DistrictPort, [
                {active, false},
                {packet, line}
            ]),
            DistrictPid = spawn(fun() -> room([], DistrictSocket) end),
            roomManager(maps:put(DistrictPort, DistrictPid, Rooms));
        {join, DistrictPort, Sender} ->
            case maps:find(DistrictPort, Rooms) of
                {ok, Pid} ->
                    Sender ! {ok_join, Pid},
                    Pid ! {enter, Sender},
                    roomManager(Rooms);
                _ ->
                    Sender ! error,
                    roomManager(Rooms)
            end;
        {contact, DistrictPort, Sender} ->
            case maps:find(DistrictPort, Rooms) of
                {ok, Pid} ->
                    Sender ! {ok_nots, Pid};
                _ ->
                    Sender ! error
            end,
            roomManager(Rooms);
        {addUser, DistrictPort, User, Sender} ->
            case maps:find(DistrictPort, Rooms) of
                {ok, Pid} ->
                    Pid ! {add_user, User, Sender},
                    roomManager(Rooms);
                _ ->
                    Sender ! error,
                    roomManager(Rooms)
            end;
        {leave, RoomPid, Sender} ->
            RoomPid ! {leave, Sender},
            Sender ! {ok_leave, roomM},
            roomManager(Rooms)
    end.

receiveFromDistrict(Pids, Socket, Pid) ->
    {ok, Data} = gen_tcp:recv(Socket, 0),
    io:format("Received: ~p~n", [Data]),
    Pid ! {ok, Data},
    io:format("enviou!~n", []),
    room(Pids, Socket).

room(Pids, Socket) ->
    receive
        {enter, Pid} ->
            Msg = <<"get_private \n">>,
            gen_tcp:send(Socket, Msg),
            io:format("passed here!~n", []),
            receiveFromDistrict(Pids, Socket, Pid),
            room([Pid | Pids], Socket);
        {add_user, User, Pid} ->
            Msg = <<<<"new ">>/binary, User/binary, <<"\n">>/binary>>,
            gen_tcp:send(Socket, Msg),
            receiveFromDistrict(Pids, Socket, Pid);
        {location, User, Location, Pid} ->
            Msg = <<<<"location ">>/binary, User/binary, <<" ">>/binary, Location/binary>>,
            gen_tcp:send(Socket, Msg),
            receiveFromDistrict(Pids, Socket, Pid);
        {number, Location, Pid} ->
            Msg = <<<<"number ">>/binary, Location/binary>>,
            gen_tcp:send(Socket, Msg),
            receiveFromDistrict(Pids, Socket, Pid);
        {notifications, Pid} ->
            gen_tcp:send(Socket, <<"notifications\n">>),
            receiveFromDistrict(Pids, Socket, Pid);
        {sick, User, Pid} ->
            Msg = <<<<"sick ">>/binary, User/binary, <<"\n">>/binary>>,
            gen_tcp:send(Socket, Msg),
            receiveFromDistrict(Pids, Socket, Pid);
        {leave, Pid} ->
            io:format("user left~n", []),
            room(Pids -- [Pid], Socket)
    end.
