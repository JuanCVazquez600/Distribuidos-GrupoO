import requests

def test_validation():
    print("=== Testing Validation for SOAP Endpoints ===")

    base_url = 'http://localhost:5000'

    # Test cases for presidents endpoint
    test_cases = [
        # Valid cases
        {'userId': 1, 'orgIds': '1,2,3', 'expected_status': 200, 'description': 'Valid orgIds'},
        {'userId': 1, 'orgIds': '1', 'expected_status': 200, 'description': 'Single valid orgId'},

        # Invalid cases
        {'userId': 1, 'orgIds': 'a,b,c', 'expected_status': 400, 'description': 'Non-integer orgIds'},
        {'userId': 1, 'orgIds': '1.5,2', 'expected_status': 400, 'description': 'Float orgIds'},
        {'userId': 1, 'orgIds': '', 'expected_status': 400, 'description': 'Empty orgIds'},
        {'userId': 'invalid', 'orgIds': '1,2', 'expected_status': 400, 'description': 'Invalid userId'},
        {'userId': 0, 'orgIds': '1,2', 'expected_status': 400, 'description': 'Zero userId'},
        {'userId': -1, 'orgIds': '1,2', 'expected_status': 400, 'description': 'Negative userId'},
        {'userId': None, 'orgIds': '1,2', 'expected_status': 400, 'description': 'Missing userId'},
        {'userId': 1, 'orgIds': None, 'expected_status': 400, 'description': 'Missing orgIds'},
    ]

    for endpoint in ['/api/soap/presidents', '/api/soap/associations']:
        print(f"\n--- Testing {endpoint} ---")
        for i, test_case in enumerate(test_cases, 1):
            try:
                data = {k: v for k, v in test_case.items() if k not in ['expected_status', 'description']}
                response = requests.post(f'{base_url}{endpoint}', json=data, timeout=5)

                status_ok = response.status_code == test_case['expected_status']
                print(f"Test {i}: {test_case['description']}")
                print(f"  Status: {response.status_code} (expected: {test_case['expected_status']}) {'✓' if status_ok else '✗'}")

                if response.status_code >= 400:
                    try:
                        error_data = response.json()
                        print(f"  Error: {error_data.get('error', 'Unknown error')}")
                    except:
                        print(f"  Response: {response.text[:100]}...")

                print()

            except requests.RequestException as e:
                print(f"Test {i}: {test_case['description']} - Connection error: {e}")

if __name__ == "__main__":
    test_validation()
