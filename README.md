# GitHub API Client

Prosta aplikacja w **Spring Boot**, która pobiera listę repozytoriów z zewnętrznego API dla wskazanego użytkownika i zwraca je w formacie JSON.

## Wymagania

- Java 25
- Maven 3.9+

## Uruchamianie aplikacji

```bash
./mvnw spring-boot:run
```
Aplikacja wystartuje na porcie 8080.

## Endpoint

**GET** `/api/{username}/repos`

Zwróci wszystkie repozytoria użytkownika, które nie są forkami.

Przykład:

GET /api/dawid101/repos


Odpowiedź (200 OK):
```json
[
    {
        "name": "cinema-reservation-app",
        "ownerLogin": "Dawid101",
        "branches": [
            {
                "name": "main",
                "sha": "541f17c609f484af93445fec0d86b1907aaac15f"
            }
        ]
    },
    {
        "name": "E_CommerceApp",
        "ownerLogin": "Dawid101",
        "branches": [
            {
                "name": "main",
                "sha": "1a1baa5429b56f7f1542e0f0bc66824f771ab572"
            }
        ]
    }
]
```

Uzytkownik nie znaleziony (404 Not Found):
```json
{
    "status": 404,
    "message": "Not Found"
}
```

## Użyte technologie

- Java 25
- Spring Boot 4.0
- Spring Web MVC
- RestClient with HttpExchange
- WireMock (integration tests)