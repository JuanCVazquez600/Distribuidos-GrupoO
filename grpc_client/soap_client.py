from zeep import Client
from zeep.transports import Transport
from zeep.plugins import HistoryPlugin
import requests
from requests.auth import HTTPBasicAuth

# Configuración del servicio SOAP
WSDL_URL = 'https://soap-app-latest.onrender.com/?wsdl'
GRUPO = 'GrupoA-TM'
CLAVE = 'clave-tm-a'

def create_soap_client():
    """Crea y configura el cliente SOAP con autenticación."""
    # Plugin para debug (opcional)
    history = HistoryPlugin()

    # Configurar transporte con timeout
    session = requests.Session()
    session.auth = HTTPBasicAuth(GRUPO, CLAVE)
    transport = Transport(session=session, timeout=10)

    # Crear cliente
    client = Client(wsdl=WSDL_URL, transport=transport, plugins=[history])
    return client, history

def get_presidents(org_ids):
    """
    Consulta presidentes de las organizaciones especificadas.

    Args:
        org_ids (list): Lista de IDs de organizaciones (strings)

    Returns:
        dict: {'success': bool, 'data': list, 'error': str}
    """
    try:
        client, history = create_soap_client()

        # Preparar parámetros
        org_ids_list = [{'string': str(org_id)} for org_id in org_ids]

        # Llamar al servicio
        response = client.service.list_presidents(org_ids=org_ids_list)

        # Parsear respuesta
        presidents = []
        if hasattr(response, 'list_presidentsResult') and response.list_presidentsResult:
            for president in response.list_presidentsResult:
                presidents.append({
                    'id': president.id,
                    'name': president.name,
                    'address': president.address,
                    'phone': president.phone,
                    'organization_id': president.organization_id
                })

        return {
            'success': True,
            'data': presidents,
            'error': None
        }

    except Exception as e:
        return {
            'success': False,
            'data': [],
            'error': str(e)
        }

def get_associations(org_ids):
    """
    Consulta datos de las organizaciones especificadas.

    Args:
        org_ids (list): Lista de IDs de organizaciones (strings)

    Returns:
        dict: {'success': bool, 'data': list, 'error': str}
    """
    try:
        client, history = create_soap_client()

        # Preparar parámetros
        org_ids_list = [{'string': str(org_id)} for org_id in org_ids]

        # Llamar al servicio
        response = client.service.list_associations(org_ids=org_ids_list)

        # Parsear respuesta
        associations = []
        if isinstance(response, list) and response:
            # Filtrar elementos None y procesar los válidos
            for association in response:
                if association is not None:
                    associations.append({
                        'id': association.id,
                        'name': association.name,
                        'address': association.address,
                        'phone': association.phone
                    })

        return {
            'success': True,
            'data': associations,
            'error': None
        }

    except Exception as e:
        return {
            'success': False,
            'data': [],
            'error': str(e)
        }
