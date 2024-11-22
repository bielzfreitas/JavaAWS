## Java by AWS

Feito com as aulas da Fernanda Kipper pela RocketSeat.


O projeto trata-se de um sistema de encurtamento de URLs utilizando a AWS como infraestrutura serveless. O objetivo é permitir que os usuários criem URLs curtas que redirecionem para URLs originais, com um tempo de expiração configurável.

O sistema é composto por duas funções Lambda:
- a primeira função é responsável por gerar e armazenar os links encurtados em um bucket S3, junto com informações como a URL original e o tempo de expiração;
- a segunda função gerencia o redirecionamento, verificando o código da URL cruta e validando se a URL ainda está dentro do prazo de expiração antes de redirecionar o usuário.

Também foi utilizado o API Gateway, para receber as requisições e encaminhar para as Lambdas (como um porteiro). Centrando tudo em um único domínio. 

Criado usando o IntelliJ + AWS Console


![Captura de tela 2024-11-21 135445](https://github.com/user-attachments/assets/84aac5b0-d82e-4b51-b22c-83c57c794968)


- No código, colocar o nome do seu bucket criado no AWS Console.
- Gerar o arquivo .jar pelo Maven ... maven clean package ... depois subir o arquivo na Lambda criada na AWS Console.
