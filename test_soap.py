from grpc_client.soap_client import get_presidents, get_associations

def test_soap_operations():
    print("=== Testing SOAP Operations ===")

    # Test operations
    print("\n=== Testing get_presidents ===")
    result = get_presidents(['6', '5'])
    print(f"Function result: {result}")

    print("\n=== Testing get_associations ===")
    result = get_associations(['6', '5'])
    print(f"Function result: {result}")

    # Test with different IDs
    print("\n=== Testing with different IDs ===")
    test_ids = [['1'], ['1', '2'], ['6', '5', '8', '10']]
    for ids in test_ids:
        print(f"\nTesting presidents with IDs {ids}:")
        result = get_presidents(ids)
        print(f"  Presidents: {result}")

        print(f"Testing associations with IDs {ids}:")
        result = get_associations(ids)
        print(f"  Associations: {result}")

    # Test REST endpoints
    print("\n=== Testing REST Endpoints ===")
    import requests

    # Test presidents endpoint
    try:
        response = requests.post('http://localhost:5000/api/soap/presidents', json={'userId': 1, 'orgIds': '6,5'})
        print(f"Presidents REST endpoint: {response.status_code}")
        if response.status_code == 200:
            print(f"Response: {response.json()}")
        else:
            print(f"Error: {response.text}")
    except Exception as e:
        print(f"Error testing presidents REST: {e}")

    # Test associations endpoint
    try:
        response = requests.post('http://localhost:5000/api/soap/associations', json={'userId': 1, 'orgIds': '6,5'})
        print(f"Associations REST endpoint: {response.status_code}")
        if response.status_code == 200:
            print(f"Response: {response.json()}")
        else:
            print(f"Error: {response.text}")
    except Exception as e:
        print(f"Error testing associations REST: {e}")

if __name__ == "__main__":
    test_soap_operations()
