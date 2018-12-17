## API

## Accounts
### Create Account 

    POST /accounts
    {
      "balance": 25.69
    }
    
    Response
    {
      "code": 0,
      "body": {
          "id": 0,
          "balance": 25.69
        }
    }

### Get Account :id
    GET /accounts/0

    Response
    {
      "code": 0,
      "body": {
          "id": 0,
          "balance": 25.69
        }
    }
    
### Get All Accounts
    GET /accounts
    
    Response
    {
    "code": 0,
    "body": [
        {
            "id": 0,
            "balance": 25.69
        },
        {
            "id": 1,
            "balance": 25.69
        }
      ]
    }

## Transactions
### Create Transaction
    POST /transactions
    {
	    "sourceAccount":"1",
	    "destinationAccount":"2",
	    "amount":"6"
    }
    
    Response
    {
    "code": 0,
    "body": {
        "id": 0,
        "sourceId": 1,
        "destinationId": 2,
        "amount": 6,
        "status": "PENDING"
      }
    }

### Get Transaction :id
    GET /transactions/0

    Response
    {
    "code": 0,
    "body": {
        "id": 0,
        "sourceId": 1,
        "destinationId": 2,
        "amount": 6,
        "status": "FAILED",
        "message": "The Destination Account is not found."
      }
    }
    
### Get All Transactions
    GET /transactions
    
    Response
    {
    "code": 0,
    "body": [
        {
            "id": 0,
            "sourceId": 1,
            "destinationId": 2,
            "amount": 6,
            "status": "FAILED",
            "message": "The Destination Account is not found."
        },
        {
            "id": 1,
            "sourceId": 1,
            "destinationId": 2,
            "amount": 6,
            "status": "EXECUTED"
         }
      ]
    }

