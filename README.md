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
          "id": 1,
          "balance": 25.69
        }
    }

### Get Account :id
    GET /accounts/1

    Response
    {
      "code": 0,
      "body": {
          "id": 1,
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
            "id": 1,
            "balance": 25.69
        },
        {
            "id": 2,
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
            "sourceId": 1,
            "destinationId": 2,
            "amount": 6,
            "status": "Transaction Completed :)"
        }
    }
