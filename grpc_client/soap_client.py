import requests
from lxml import etree

# Configuración del servicio SOAP
SOAP_URL = 'https://soap-app-latest.onrender.com/'
GRUPO = 'GrupoA-TM'
CLAVE = 'clave-tm-a'

def get_presidents(org_ids):
    """
    Consulta presidentes de las organizaciones especificadas usando requests con XML raw.

    Args:
        org_ids (list): Lista de IDs de organizaciones (strings)

    Returns:
        dict: {'success': bool, 'data': list, 'error': str}
    """
    try:
        # Construir XML dinámicamente
        org_ids_xml = ''.join(f'<tns:string>{str(org_id)}</tns:string>' for org_id in org_ids)

        soap_request = f"""<?xml version="1.0" encoding="utf-8"?>
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:auth="auth.headers" xmlns:tns="soap.backend">
  <soapenv:Header>
    <auth:Auth>
      <auth:Grupo>{GRUPO}</auth:Grupo>
      <auth:Clave>{CLAVE}</auth:Clave>
    </auth:Auth>
  </soapenv:Header>
  <soapenv:Body>
    <tns:list_presidents>
      <tns:org_ids>
        {org_ids_xml}
      </tns:org_ids>
    </tns:list_presidents>
  </soapenv:Body>
</soapenv:Envelope>"""

        headers = {
            'Content-Type': 'text/xml; charset=utf-8',
            'SOAPAction': 'list_presidents'
        }

        response = requests.post(SOAP_URL, data=soap_request, headers=headers, timeout=30)

        if response.status_code != 200:
            return {
                'success': False,
                'data': [],
                'error': f'HTTP {response.status_code}: {response.text}'
            }

        # Parsear respuesta XML
        root = etree.fromstring(response.content)
        ns = {'soap': 'http://schemas.xmlsoap.org/soap/envelope/', 'tns': 'soap.backend', 's0': 'models'}

        presidents = []
        result = root.xpath('//tns:list_presidentsResult', namespaces=ns)
        if result:
            for president_elem in result[0].xpath('s0:PresidentType', namespaces=ns):
                president = {
                    'id': int(president_elem.xpath('s0:id/text()', namespaces=ns)[0]),
                    'name': president_elem.xpath('s0:name/text()', namespaces=ns)[0],
                    'address': president_elem.xpath('s0:address/text()', namespaces=ns)[0],
                    'phone': president_elem.xpath('s0:phone/text()', namespaces=ns)[0],
                    'organization_id': int(president_elem.xpath('s0:organization_id/text()', namespaces=ns)[0])
                }
                presidents.append(president)

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
    Consulta datos de las organizaciones especificadas usando requests con XML raw.

    Args:
        org_ids (list): Lista de IDs de organizaciones (strings)

    Returns:
        dict: {'success': bool, 'data': list, 'error': str}
    """
    try:
        # Construir XML dinámicamente
        org_ids_xml = ''.join(f'<tns:string>{str(org_id)}</tns:string>' for org_id in org_ids)

        soap_request = f"""<?xml version="1.0" encoding="utf-8"?>
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:auth="auth.headers" xmlns:tns="soap.backend">
  <soapenv:Header>
    <auth:Auth>
      <auth:Grupo>{GRUPO}</auth:Grupo>
      <auth:Clave>{CLAVE}</auth:Clave>
    </auth:Auth>
  </soapenv:Header>
  <soapenv:Body>
    <tns:list_associations>
      <tns:org_ids>
        {org_ids_xml}
      </tns:org_ids>
    </tns:list_associations>
  </soapenv:Body>
</soapenv:Envelope>"""

        headers = {
            'Content-Type': 'text/xml; charset=utf-8',
            'SOAPAction': 'list_associations'
        }

        response = requests.post(SOAP_URL, data=soap_request, headers=headers, timeout=30)

        if response.status_code != 200:
            return {
                'success': False,
                'data': [],
                'error': f'HTTP {response.status_code}: {response.text}'
            }

        # Parsear respuesta XML
        root = etree.fromstring(response.content)
        ns = {'soap': 'http://schemas.xmlsoap.org/soap/envelope/', 'tns': 'soap.backend', 's0': 'models'}

        associations = []
        result = root.xpath('//tns:list_associationsResult', namespaces=ns)
        if result:
            for assoc_elem in result[0].xpath('s0:OrganizationType', namespaces=ns):
                association = {
                    'id': int(assoc_elem.xpath('s0:id/text()', namespaces=ns)[0]),
                    'name': assoc_elem.xpath('s0:name/text()', namespaces=ns)[0],
                    'address': assoc_elem.xpath('s0:address/text()', namespaces=ns)[0],
                    'phone': assoc_elem.xpath('s0:phone/text()', namespaces=ns)[0]
                }
                associations.append(association)

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
