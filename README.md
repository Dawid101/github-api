# GitHub API Client (Spring Boot)

Prosta aplikacja w **Spring Boot**, która pobiera listę repozytoriów z zewnętrznego API dla wskazanego użytkownika i zwraca je w formacie JSON.

## 🚀 Funkcjonalność

- Pobieranie repozytoriów użytkownika przez REST API
- Wykorzystanie Spring Boot do obsługi HTTP i komunikacji z API
- Zwracanie danych w formacie JSON


## 📍 Endpoint

**GET** `/api/{username}/repos`

Przykład:

GET /api/dawid101/repos


Odpowiedź:
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
