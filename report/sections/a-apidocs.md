\newpage

\appendix

# Documentação da REST API {#sec:apidocs}

## Distritos {-}

**_GET_** `/districts`

> **Parâmetros**

- `sort`: Atributo utilizado na ordenação. [Valor por omissão: `ratio`.]
- `limit`: Máximo de elementos a receber. [Valor por omissão: `5`.]

> **Exemplos de pedidos**

```bash
$ http GET :8090/api/v1/districts\?sort=users\&limit=2
```

**Status**: `200 OK`

```json
[
  {
    "name": "Lisboa",
    "users": 500,
    "infected": 250,
    "contacted": 5.0,
    "ratio": 0.5
  },
  {
    "name": "Braga",
    "users": 200,
    "infected": 15,
    "contacted": 5.0,
    "ratio": 0.075
  }
]
```

```bash
$ http GET :8090/api/v1/districts\?sort=fake\&limit=2
```

**Status**: `400 BAD REQUEST`

```json
{
  "code": 400,
  "message": "Sort field isn't supported"
}
```

---

**GET** `/districts/{district}`

> **Parâmetros**

- `district`: Chave do distrito. [Exemplo: braga, ilha_da_madeira]

> **Exemplos de pedidos**

```bash
$ http GET :8090/api/v1/districts/braga
```

**Status**: `200 OK`

```json
{
  "name": "Braga",
  "users": 200,
  "infected": 15,
  "contacted": 5.0,
  "ratio": 0.075
}
```

```bash
$ http GET :8090/api/v1/districts/vieira_do_minho
```

**Status**: `404 NOT FOUND`

```json
{
  "code": 404,
  "message": "District doesn't exist"
}
```

---

**POST** `/districts`

> **Corpo do pedido**

Um objeto do tipo `Distrito` em JSON.

```json
{
  "name": "Porto",
  "users": 10,
  "infected": 5,
  "contacted": 1.0
}
```

> **Exemplos de pedidos**

```bash
$ http POST :8090/api/v1/districts name:="aveiro" contacted:=2 infected:=200 users:=6000
```

**Status**: `200 OK`

```json
{
  "name": "Porto",
  "users": 6000,
  "infected": 200,
  "contacted": 2.0
}
```

```bash
$ http POST :8090/api/v1/districts name:="aveiro" contacted:=2 infected:=500 users:=8000
```

**Status**: `409 CONFLICT`

```json
{
  "code": 409,
  "message": "District already exists"
}
```

---

**PUT** `/districts/{district}`

> **Parâmetros**

- `district`: Chave do distrito. [Exemplo: lisboa, vila_real]

> **Corpo do pedido**

Um objeto do tipo `Distrito` em JSON.

```json
{
  "name": "Faro",
  "users": 80,
  "infected": 4,
  "contacted": 0.5
}
```

> **Exemplos de pedidos**

```bash
$ http PUT :8090/api/v1/districts/coimbra name="coimbra" users:=35 infected:=2 contacted:=1
```

**Status**: `200 OK`

```json
{
  "name": "Coimbra",
  "users": 35,
  "infected": 2,
  "contacted": 1.0
}
```

**Status**: `404 NOT FOUND`

```json
{
  "code": 404,
  "message": "District doesn't exist"
}
```

---

**DELETE** `/districts/{district}`

> **Parâmetros**

- `district`: Chave do distrito. [Exemplo: guarda, beja]

> **Exemplos de pedidos**

```bash
$ http DELETE :8090/api/v1/districts/portalegre
```

**Status**: `200 OK`

**Status**: `404 NOT FOUND`

```json
{
  "code": 404,
  "message": "District doesn't exist"
}
```

## Localizações {-}

**_GET_** `/locations`

> **Parâmetros**

- `sort`: Atributo utilizado na ordenação. [Valor por omissão: `crowding`.]
- `limit`: Máximo de elementos a receber. [Valor por omissão: `5`.]

> **Exemplos de pedidos**

```bash
$ http GET :8090/api/v1/locations\?sort=crowding\&limit=1
```

**Status**: `200 OK`

```json
[
  {
    "id": 12314123,
    "district": "Castelo Branco",
    "x": 10,
    "y": 20,
    "crowding": 8
  }
]
```

```bash
$ http GET :8090/api/v1/locations\?sort=fake\&limit=2
```

**Status**: `400 BAD REQUEST`

```json
{
  "code": 400,
  "message": "Sort field isn't supported"
}
```

---

**GET** `/locations/{id}`

> **Parâmetros**

- `id`: ID da localização. [Exemplo: 123141, 21412]

> **Exemplos de pedidos**

```bash
$ http GET :8090/api/v1/location/123456
```

**Status**: `200 OK`

```json
{
  "id": 123456,
  "district": "Castelo Branco",
  "x": 1,
  "y": 4,
  "crowding": 20
}
```

```bash
$ http GET :8090/api/v1/location/00000
```

**Status**: `404 NOT FOUND`

```json
{
  "code": 404,
  "message": "Location doesn't exist"
}
```

---

**POST** `/locations`

> **Corpo do pedido**

Um objeto do tipo `Location` em JSON.

```json
{
  "id": 4321234,
  "district": "Guarda",
  "x": 1,
  "y": 4,
  "crowding": 25
}
```

> **Exemplos de pedidos**

```bash
$ http POST :8090/api/v1/locations name:="aveiro" x:=2 y:=5 crowding:=8
```

**Status**: `200 OK`

```json
{
  "id": 84321,
  "district": "Aveiro",
  "x": 2,
  "y": 5,
  "crowding": 8
}
```

```bash
$ http POST :8090/api/v1/locations name:="aveiro" x:=2 y:=5 crowding:=8
```

**Status**: `409 CONFLICT`

```json
{
  "code": 409,
  "message": "Location already exists"
}
```

---

**PUT** `/locations/{id}`

> **Parâmetros**

- `id`: ID da localização. [Exemplo: 1234567, 987654]

> **Corpo do pedido**

Um objeto do tipo `Location` em JSON.

```json
{
  "id": 424242,
  "district": "Santarém",
  "x": 4,
  "y": 2,
  "crowding": 42
}
```

> **Exemplos de pedidos**

```bash
$ http PUT :8090/api/v1/locations/33434 name="setubal" x:=4 y:=8 crowding:=9
```

**Status**: `200 OK`

```json
{
  "id": 33434,
  "district": "Setubal",
  "x": 4,
  "y": 8,
  "crowding": 9
}
```

---

**DELETE** `/locations/{id}`

> **Parâmetros**

- `id`: ID da localização. [Exemplo: 776622, 908422]

> **Exemplos de pedidos**

```bash
$ http DELETE :8090/api/v1/locations/21321
```

**Status**: `200 OK`

**Status**: `404 NOT FOUND`

```json
{
  "code": 404,
  "message": "District doesn't exist"
}
```
