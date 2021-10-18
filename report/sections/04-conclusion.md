# Conclusão {#sec:end}

A plataforma de rastreamento de propagação do vírus COVID-19 elaborada é
capaz de rastrear os contactos feitos entre pessoas e é capaz de notificar
todos os contactos que estiveram em contacto com algum infectado. Permite
ainda a subscrição de cada cliente a eventos como uma nova localização estar
vazia ou menos lotada. Além disso, fornece uma REST API pública que permite
consultar estatísticas por distrito e por localização.

O cliente foi desenvolvido para ser utilizado através de uma interface
gráfica intuitiva capaz de interagir com todas as componentes do sistema sem
necessitar de se conhecer o funcionamento interno de cada componente. Este
requisito contribuí para um melhor sistema, uma vez que a facilidade de
utilização é importante para que o maior grupo possível de pessoas seja capaz
de utilizar.

As decisões arquiteturais que levaram a que o _frontend_ fosse o canal de
comunicação direta entre os clientes e o sistema permitiram que o
desenvolvimento dos servidores distritais fosse simplificado nas questões de
concorrência. Assim, a gestão complexa das conexões de todos os clientes a um
servidor distrital é tratada pelo servidor de _frontend_. Abdicar deste
tratamento implicaria tornar os servidores distritais _thread safe_ para evitar
os vários problemas de memória partilhada e os ganhos de desempenho seriam
mínimos.

A expansão deste projeto visa um maior trabalho na obtenção da localização de
cada cliente. Neste protótipo assumiu-se uma simplificação das localizações
como uma matriz $N \times N$ que poderá ser abstraída para coordenadas
geográficas reais que deverão ser obtidas automáticamente pelo sistema de GPS
dos dispositivos.
