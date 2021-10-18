#!/usr/bin/env escript

main(_) ->
    ServerPort = os:getenv("APP_SERVER_PORT", "8080"),
    {Port, _} = string:to_integer(ServerPort),
    frontend_server:start_server(Port).
