from grpc_client.soap_client import create_soap_client, get_presidents, get_associations

def test_soap_operations():
    print("=== Testing SOAP Operations ===")

    # Test client creation
    try:
        client, history = create_soap_client()
        print("✅ SOAP client created successfully")
    except Exception as e:
        print(f"❌ Failed to create SOAP client: {e}")
        return

    # Check available operations
    print("\nAvailable operations:")
    for service in client.wsdl.services.values():
        for port in service.ports.values():
            for operation_name in port.binding._operations.keys():
                print(f" - {operation_name}")

    # Test operations with debug info
    print("\n=== Testing get_presidents ===")
    try:
        client, history = create_soap_client()
        # Test direct call
        print("Testing direct SOAP call for presidents...")
        response = client.service.list_presidents(org_ids=[{'string': '6'}, {'string': '5'}])
        print(f"Direct response: {response}")
        print(f"Response type: {type(response)}")
        if hasattr(response, '__dict__'):
            print(f"Response attributes: {response.__dict__}")
        # Check history for last request/response
        if history.last_sent and history.last_received:
            print("Last sent:")
            print(history.last_sent['envelope'])
            print("Last received:")
            print(history.last_received['envelope'])
    except Exception as e:
        print(f"Direct call failed: {e}")
        import traceback
        traceback.print_exc()

    result = get_presidents(['6', '5'])
    print(f"Function result: {result}")

    print("\n=== Testing get_associations ===")
    try:
        client, history = create_soap_client()
        # Test direct call
        print("Testing direct SOAP call for associations...")
        response = client.service.list_associations(org_ids=[{'string': '6'}, {'string': '5'}])
        print(f"Direct response: {response}")
        print(f"Response type: {type(response)}")
        if hasattr(response, '__dict__'):
            print(f"Response attributes: {response.__dict__}")
        # Check history for last request/response
        if history.last_sent and history.last_received:
            print("Last sent:")
            print(history.last_sent['envelope'])
            print("Last received:")
            print(history.last_received['envelope'])
    except Exception as e:
        print(f"Direct call failed: {e}")
        import traceback
        traceback.print_exc()

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

if __name__ == "__main__":
    test_soap_operations()
