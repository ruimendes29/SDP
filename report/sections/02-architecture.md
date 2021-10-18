# Arquitetura do Sistema {#sec:architecture}

![Diagrama da Arquitetura do Sistema](figures/architecture.png){ #fig:architecture }

A @fig:architecture ilustra a arquitetura do sistema expondo ligações uni e
bidirecionais entre os vários componentes.

Os pedidos dos clientes mais típicos, como a mudança de localização ou
pedidos de subscrição, são feitos ao servidor de _frontend_ que
posteriormente reencaminha os pedidos aos servidores distritais, sendo
garantida a autenticação por este.

O único contacto direto entre os clientes e os servidores distritais é na
obtenção das notificações aos eventos por estes subscritos.

Os servidores distritais comunicam com o diretório para o irem atualizando
com as estatísticas mais recentes. E os clientes comunicam diretamente com o
diretório para as consultarem.
