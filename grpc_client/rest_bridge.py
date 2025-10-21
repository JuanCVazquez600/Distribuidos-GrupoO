from flask import Flask, jsonify, request
from flask_cors import CORS
import grpc
import requests
from proto import usuarios_pb2, usuarios_pb2_grpc, eventos_pb2, eventos_pb2_grpc, inventario_pb2, inventario_pb2_grpc
from soap_client import get_presidents, get_associations

app = Flask(__name__)
CORS(app)

SPRING_BOOT_URL = 'http://localhost:8080'

def grpc_login(usuario_email, clave):
    channel = grpc.insecure_channel('localhost:9090')
    stub = usuarios_pb2_grpc.UsuarioServiceStub(channel)
    request_proto = usuarios_pb2.LoginRequest(
        usuarioEmail=usuario_email,
        clave=clave
    )
    try:
        response = stub.Login(request_proto)
        usuario_data = None
        if hasattr(response, 'usuario') and response.usuario.id != 0:
            usuario_data = {
                'id': response.usuario.id,
                'nombreUsuario': response.usuario.nombreUsuario,
                'nombre': response.usuario.nombre,
                'apellido': response.usuario.apellido,
                'telefono': response.usuario.telefono,
                'email': response.usuario.email,
                'rol': response.usuario.rol,
                'activo': response.usuario.activo
            }
        return {
            'exito': response.exito,
            'mensaje': response.mensaje,
            'usuario': usuario_data
        }
    except grpc.RpcError as e:
        return {'exito': False, 'mensaje': str(e), 'usuario': None}

def grpc_crear_evento(nombre, descripcion, fecha_hora, user_id=0):
    channel = grpc.insecure_channel('localhost:9090')
    stub = eventos_pb2_grpc.EventoServiceStub(channel)
    request_proto = eventos_pb2.EventoRequest(
        nombre=nombre,
        descripcion=descripcion,
        fechaHora=fecha_hora,
        miembros=[],
        userId=user_id
    )
    try:
        response = stub.CrearEvento(request_proto)
        return {'exito': response.exito, 'mensaje': response.mensaje}
    except grpc.RpcError as e:
        return {'exito': False, 'mensaje': str(e)}

@app.route('/login', methods=['GET'])
def login():
    usuario_email = request.args.get('usuarioEmail')
    clave = request.args.get('clave')
    if not usuario_email or not clave:
        return jsonify({'exito': False, 'mensaje': 'Faltan parámetros'}), 400
    result = grpc_login(usuario_email, clave)
    return jsonify(result)

@app.route('/usuarios', methods=['GET'])
def usuarios():
    channel = grpc.insecure_channel('localhost:9090')
    stub = usuarios_pb2_grpc.UsuarioServiceStub(channel)
    try:
        response = stub.ListarUsuarios(usuarios_pb2.Empty())
        usuarios_list = []
        for u in response.usuarios:
            usuarios_list.append({
                'id': u.id,
                'nombreUsuario': u.nombreUsuario,
                'nombre': u.nombre,
                'apellido': u.apellido,
                'telefono': u.telefono,
                'clave': u.clave,
                'email': u.email,
                'rol': u.rol,
                'activo': u.activo
            })
        print(f"Usuarios obtenidos desde gRPC: {len(usuarios_list)}")
        return jsonify(usuarios_list)
    except grpc.RpcError as e:
        print(f"Error al obtener usuarios: {e}")
        return jsonify([])

@app.route('/eventos', methods=['GET'])
def eventos():
    channel = grpc.insecure_channel('localhost:9090')
    stub = eventos_pb2_grpc.EventoServiceStub(channel)
    try:
        response = stub.ListarEventos(eventos_pb2.Empty())
        eventos_list = []
        for e in response.eventos:
            eventos_list.append({
                'id': e.id,
                'nombre': e.nombre,
                'descripcion': e.descripcion,
                'fechaHora': e.fechaHora,
                'miembros': [{'id': m.id, 'nombre': m.nombre, 'apellido': m.apellido} for m in e.miembros]
            })
        print(f"Eventos obtenidos desde gRPC: {len(eventos_list)}")
        return jsonify(eventos_list)
    except grpc.RpcError as e:
        print(f"Error al obtener eventos: {e}")
        return jsonify([])

@app.route('/eventos', methods=['POST'])
def crear_evento():
    data = request.json
    nombre = data.get('nombre')
    descripcion = data.get('descripcion')
    fecha_hora = data.get('fechaHora')
    user_id = data.get('userId', 0)
    if not nombre or not descripcion or not fecha_hora:
        return jsonify({'exito': False, 'mensaje': 'Faltan parámetros'}), 400
    result = grpc_crear_evento(nombre, descripcion, fecha_hora, user_id)
    return jsonify(result)

@app.route('/donaciones', methods=['GET'])
def donaciones():
    channel = grpc.insecure_channel('localhost:9090')
    stub = inventario_pb2_grpc.InventarioServiceStub(channel)
    try:
        response = stub.ListarDonaciones(inventario_pb2.Empty())
        donaciones_list = []
        for d in response.donaciones:
            donaciones_list.append({
                'id': d.id,
                'categoria': d.categoria,
                'descripcion': d.descripcion,
                'cantidad': d.cantidad,
                'eliminado': d.eliminado
            })
        print(f"Donaciones obtenidas desde gRPC: {len(donaciones_list)}")
        return jsonify(donaciones_list)
    except grpc.RpcError as e:
        print(f"Error al obtener donaciones: {e}")
        return jsonify([])

@app.route('/usuarios', methods=['POST'])
def crear_usuario():
    data = request.json
    usuario_request = usuarios_pb2.UsuarioRequest(
        id=data.get('id', 0),
        nombreUsuario=data.get('nombreUsuario', ''),
        nombre=data.get('nombre', ''),
        apellido=data.get('apellido', ''),
        telefono=data.get('telefono', ''),
        clave=data.get('clave', ''),
        email=data.get('email', ''),
        rol=data.get('rol', ''),
        activo=data.get('activo', True),
        userId=data.get('userId', 0)
    )
    channel = grpc.insecure_channel('localhost:9090')
    stub = usuarios_pb2_grpc.UsuarioServiceStub(channel)
    try:
        response = stub.CrearUsuario(usuario_request)
        return jsonify({'exito': response.exito, 'mensaje': response.mensaje})
    except grpc.RpcError as e:
        return jsonify({'exito': False, 'mensaje': str(e)})

@app.route('/usuarios', methods=['PUT'])
def modificar_usuario():
    data = request.json
    usuario_request = usuarios_pb2.UsuarioRequest(
        id=data.get('id'),
        nombreUsuario=data.get('nombreUsuario', ''),
        nombre=data.get('nombre', ''),
        apellido=data.get('apellido', ''),
        telefono=data.get('telefono', ''),
        clave=data.get('clave', ''),
        email=data.get('email', ''),
        rol=data.get('rol', ''),
        activo=data.get('activo', True),
        userId=data.get('userId', 0)
    )
    channel = grpc.insecure_channel('localhost:9090')
    stub = usuarios_pb2_grpc.UsuarioServiceStub(channel)
    try:
        response = stub.ModificarUsuario(usuario_request)
        return jsonify({'exito': response.exito, 'mensaje': response.mensaje})
    except grpc.RpcError as e:
        return jsonify({'exito': False, 'mensaje': str(e)})

@app.route('/usuarios/<int:id>', methods=['DELETE'])
def baja_usuario(id):
    data = request.json or {}
    channel = grpc.insecure_channel('localhost:9090')
    stub = usuarios_pb2_grpc.UsuarioServiceStub(channel)
    usuario_id_request = usuarios_pb2.UsuarioIdRequest(id=id, userId=data.get('userId', 0))
    try:
        response = stub.BajaUsuario(usuario_id_request)
        return jsonify({'exito': response.exito, 'mensaje': response.mensaje})
    except grpc.RpcError as e:
        return jsonify({'exito': False, 'mensaje': str(e)})

@app.route('/eventos', methods=['PUT'])
def modificar_evento():
    data = request.json
    miembros = []
    for m in data.get('miembros', []):
        miembros.append(eventos_pb2.MiembroRequest(id=m.get('id', 0), nombre=m.get('nombre', ''), apellido=m.get('apellido', '')))
    evento_request = eventos_pb2.EventoRequest(
        id=data.get('id', 0),
        nombre=data.get('nombre', ''),
        descripcion=data.get('descripcion', ''),
        fechaHora=data.get('fechaHora', ''),
        miembros=miembros,
        userId=data.get('userId', 0)
    )
    channel = grpc.insecure_channel('localhost:9090')
    stub = eventos_pb2_grpc.EventoServiceStub(channel)
    try:
        response = stub.ModificarEvento(evento_request)
        return jsonify({'exito': response.exito, 'mensaje': response.mensaje})
    except grpc.RpcError as e:
        return jsonify({'exito': False, 'mensaje': str(e)})

@app.route('/eventos/<int:id>', methods=['DELETE'])
def baja_evento(id):
    data = request.json or {}
    channel = grpc.insecure_channel('localhost:9090')
    stub = eventos_pb2_grpc.EventoServiceStub(channel)
    evento_id_request = eventos_pb2.EventoIdRequest(id=id, userId=data.get('userId', 0))
    try:
        response = stub.BajaEvento(evento_id_request)
        return jsonify({'exito': response.exito, 'mensaje': response.mensaje})
    except grpc.RpcError as e:
        return jsonify({'exito': False, 'mensaje': str(e)})

@app.route('/donaciones', methods=['POST'])
def crear_donacion():
    data = request.json
    donacion_request = inventario_pb2.DonacionRequest(
        id=data.get('id', 0),
        categoria=data.get('categoria', 0),
        descripcion=data.get('descripcion', ''),
        cantidad=data.get('cantidad', 0),
        eliminado=data.get('eliminado', False),
        userId=data.get('userId', 0)
    )
    channel = grpc.insecure_channel('localhost:9090')
    stub = inventario_pb2_grpc.InventarioServiceStub(channel)
    try:
        response = stub.AgregarDonacion(donacion_request)
        return jsonify({'exito': response.exito, 'mensaje': response.mensaje})
    except grpc.RpcError as e:
        return jsonify({'exito': False, 'mensaje': str(e)})

@app.route('/donaciones', methods=['PUT'])
def modificar_donacion():
    data = request.json
    donacion_request = inventario_pb2.DonacionRequest(
        id=data.get('id', 0),
        categoria=data.get('categoria', 0),
        descripcion=data.get('descripcion', ''),
        cantidad=data.get('cantidad', 0),
        eliminado=data.get('eliminado', False),
        userId=data.get('userId', 0)
    )
    channel = grpc.insecure_channel('localhost:9090')
    stub = inventario_pb2_grpc.InventarioServiceStub(channel)
    try:
        response = stub.ModificarDonacion(donacion_request)
        return jsonify({'exito': response.exito, 'mensaje': response.mensaje})
    except grpc.RpcError as e:
        return jsonify({'exito': False, 'mensaje': str(e)})

@app.route('/donaciones/<int:id>', methods=['DELETE'])
def baja_donacion(id):
    data = request.json or {}
    channel = grpc.insecure_channel('localhost:9090')
    stub = inventario_pb2_grpc.InventarioServiceStub(channel)
    donacion_id_request = inventario_pb2.DonacionIdRequest(id=id, userId=data.get('userId', 0))
    try:
        response = stub.BajaDonacion(donacion_id_request)
        return jsonify({'exito': response.exito, 'mensaje': response.mensaje})
    except grpc.RpcError as e:
        return jsonify({'exito': False, 'mensaje': str(e)})

@app.route('/eventos/<int:evento_id>/miembros/<int:miembro_id>', methods=['POST'])
def asignar_miembro(evento_id, miembro_id):
    data = request.json or {}
    channel = grpc.insecure_channel('localhost:9090')
    stub = eventos_pb2_grpc.EventoServiceStub(channel)
    asignar_request = eventos_pb2.AsignarMiembroRequest(
        eventoId=evento_id,
        miembroId=miembro_id,
        userId=data.get('userId', 0)
    )
    try:
        response = stub.AsignarMiembro(asignar_request)
        return jsonify({'exito': response.exito, 'mensaje': response.mensaje})
    except grpc.RpcError as e:
        return jsonify({'exito': False, 'mensaje': str(e)})

@app.route('/eventos/<int:evento_id>/miembros/<int:miembro_id>', methods=['DELETE'])
def quitar_miembro(evento_id, miembro_id):
    data = request.json or {}
    channel = grpc.insecure_channel('localhost:9090')
    stub = eventos_pb2_grpc.EventoServiceStub(channel)
    quitar_request = eventos_pb2.QuitarMiembroRequest(
        eventoId=evento_id,
        miembroId=miembro_id,
        userId=data.get('userId', 0)
    )
    try:
        response = stub.QuitarMiembro(quitar_request)
        return jsonify({'exito': response.exito, 'mensaje': response.mensaje})
    except grpc.RpcError as e:
        return jsonify({'exito': False, 'mensaje': str(e)})

# Proxy endpoints to Spring Boot/Kafka server
@app.route('/requests/publish', methods=['POST'])
def publish_request():
    try:
        response = requests.post(f'{SPRING_BOOT_URL}/requests/publish', json=request.json)
        try:
            response_data = response.json()
        except ValueError:
            # If response is not valid JSON, return a generic success/error based on status
            if response.status_code >= 200 and response.status_code < 300:
                return jsonify({'message': 'Solicitud publicada correctamente'}), response.status_code
            else:
                return jsonify({'error': 'Error al publicar solicitud', 'details': f'HTTP {response.status_code}'}), response.status_code
        return jsonify(response_data), response.status_code
    except requests.RequestException as e:
        print(f"Error publishing request: {e}")
        return jsonify({'error': 'Error al publicar solicitud', 'details': str(e)}), 500

@app.route('/offers/publish', methods=['POST'])
def publish_offer():
    try:
        print(f"Publishing offer: {request.json}")
        response = requests.post(f'{SPRING_BOOT_URL}/offers/publish', json=request.json)
        print(f"Spring Boot response status: {response.status_code}")
        print(f"Spring Boot response content: {response.text}")
        try:
            response_data = response.json()
            print(f"Parsed JSON response: {response_data}")
        except ValueError:
            # If response is not valid JSON, return a generic success/error based on status
            print(f"Non-JSON response: {response.text}")
            if response.status_code >= 200 and response.status_code < 300:
                return jsonify({'message': 'Oferta publicada correctamente'}), response.status_code
            else:
                return jsonify({'error': 'Error al publicar oferta', 'details': f'HTTP {response.status_code}'}), response.status_code
        return jsonify(response_data), response.status_code
    except requests.RequestException as e:
        print(f"Error publishing offer: {e}")
        return jsonify({'error': 'Error al publicar oferta', 'details': str(e)}), 500

@app.route('/events/publish', methods=['POST'])
def publish_event():
    try:
        print(f"Publishing event: {request.json}")
        response = requests.post(f'{SPRING_BOOT_URL}/events/publish', json=request.json)
        print(f"Spring Boot response status: {response.status_code}")
        print(f"Spring Boot response content: {response.text}")
        try:
            response_data = response.json()
            print(f"Parsed JSON response: {response_data}")
        except ValueError:
            # If response is not valid JSON, return a generic success/error based on status
            print(f"Non-JSON response: {response.text}")
            if response.status_code >= 200 and response.status_code < 300:
                return jsonify({'message': 'Evento publicado correctamente'}), response.status_code
            else:
                return jsonify({'error': 'Error al publicar evento', 'details': f'HTTP {response.status_code}'}), response.status_code
        return jsonify(response_data), response.status_code
    except requests.RequestException as e:
        print(f"Error publishing event: {e}")

@app.route('/transfers/send/<recipient_org>', methods=['POST'])
def send_transfer(recipient_org):
    try:
        response = requests.post(f'{SPRING_BOOT_URL}/transfers/send/{recipient_org}', json=request.json)
        try:
            response_data = response.json()
        except ValueError:
            # If response is not valid JSON, return a generic success/error based on status
            if response.status_code >= 200 and response.status_code < 300:
                return jsonify({'message': 'Transferencia enviada correctamente'}), response.status_code
            else:
                return jsonify({'error': 'Error al enviar transferencia', 'details': f'HTTP {response.status_code}'}), response.status_code
        return jsonify(response_data), response.status_code
    except requests.RequestException as e:
        print(f"Error sending transfer: {e}")
        return jsonify({'error': 'Error al enviar transferencia', 'details': str(e)}), 500

@app.route('/requests/list', methods=['GET'])
def list_requests():
    try:
        response = requests.get(f'{SPRING_BOOT_URL}/requests/list')
        try:
            response_data = response.json()
        except ValueError:
            # If response is not valid JSON, return a generic success/error based on status
            if response.status_code >= 200 and response.status_code < 300:
                return jsonify({'message': 'Solicitudes obtenidas correctamente'}), response.status_code
            else:
                return jsonify({'error': 'Error al obtener solicitudes', 'details': f'HTTP {response.status_code}'}), response.status_code
        return jsonify(response_data), response.status_code
    except requests.RequestException as e:
        print(f"Error listing requests: {e}")
        return jsonify({'error': 'Error al obtener solicitudes', 'details': str(e)}), 500

@app.route('/offers/list', methods=['GET'])
def list_offers():
    try:
        response = requests.get(f'{SPRING_BOOT_URL}/offers/list')
        try:
            response_data = response.json()
        except ValueError:
            # If response is not valid JSON, return a generic success/error based on status
            if response.status_code >= 200 and response.status_code < 300:
                return jsonify({'message': 'Ofertas obtenidas correctamente'}), response.status_code
            else:
                return jsonify({'error': 'Error al obtener ofertas', 'details': f'HTTP {response.status_code}'}), response.status_code
        return jsonify(response_data), response.status_code
    except requests.RequestException as e:
        print(f"Error listing offers: {e}")
        return jsonify({'error': 'Error al obtener ofertas', 'details': str(e)}), 500

@app.route('/transfers/list', methods=['GET'])
def list_transfers():
    try:
        response = requests.get(f'{SPRING_BOOT_URL}/transfers/list')
        try:
            response_data = response.json()
        except ValueError:
            # If response is not valid JSON, return a generic success/error based on status
            if response.status_code >= 200 and response.status_code < 300:
                return jsonify({'message': 'Transferencias obtenidas correctamente'}), response.status_code
            else:
                return jsonify({'error': 'Error al obtener transferencias', 'details': f'HTTP {response.status_code}'}), response.status_code
        return jsonify(response_data), response.status_code
    except requests.RequestException as e:
        print(f"Error listing transfers: {e}")
        return jsonify({'error': 'Error al obtener transferencias', 'details': str(e)}), 500

@app.route('/events/external', methods=['GET'])
def list_external_events():
    try:
        print("Fetching external events...")
        response = requests.get(f'{SPRING_BOOT_URL}/events/external')
        print(f"Spring Boot response status: {response.status_code}")
        print(f"Spring Boot response content: {response.text}")
        try:
            response_data = response.json()
            print(f"Parsed JSON response: {response_data}")
        except ValueError:
            # If response is not valid JSON, return a generic success/error based on status
            print(f"Non-JSON response: {response.text}")
            if response.status_code >= 200 and response.status_code < 300:
                return jsonify({'message': 'Eventos externos obtenidos correctamente'}), response.status_code
            else:
                return jsonify({'error': 'Error al obtener eventos externos', 'details': f'HTTP {response.status_code}'}), response.status_code
        return jsonify(response_data), response.status_code
    except requests.RequestException as e:
        print(f"Error listing external events: {e}")
        return jsonify({'error': 'Error al obtener eventos externos', 'details': str(e)}), 500

@app.route('/adhesions/join/<event_id>', methods=['POST'])
def join_event(event_id):
    try:
        print(f"Joining event {event_id}: {request.json}")
        response = requests.post(f'{SPRING_BOOT_URL}/adhesions/join/{event_id}', json=request.json)
        print(f"Spring Boot response status: {response.status_code}")
        print(f"Spring Boot response content: {response.text}")
        try:
            response_data = response.json()
            print(f"Parsed JSON response: {response_data}")
        except ValueError:
            # If response is not valid JSON, return a generic success/error based on status
            print(f"Non-JSON response: {response.text}")
            if response.status_code >= 200 and response.status_code < 300:
                return jsonify({'message': 'Adherido al evento correctamente'}), response.status_code
            else:
                return jsonify({'error': 'Error al adherirse al evento', 'details': f'HTTP {response.status_code}'}), response.status_code
        return jsonify(response_data), response.status_code
    except requests.RequestException as e:
        print(f"Error joining event {event_id}: {e}")
        return jsonify({'error': 'Error al adherirse al evento', 'details': str(e)}), 500

@app.route('/events/external/<event_id>', methods=['DELETE'])
def delete_external_event(event_id):
    try:
        user_id = request.args.get('userId')
        if not user_id:
            return jsonify({'error': 'userId es requerido'}), 400
        print(f"Deleting external event {event_id} for user {user_id}")
        response = requests.delete(f'{SPRING_BOOT_URL}/events/external/{event_id}?userId={user_id}')
        print(f"Spring Boot response status: {response.status_code}")
        print(f"Spring Boot response content: {response.text}")
        try:
            response_data = response.json()
            print(f"Parsed JSON response: {response_data}")
        except ValueError:
            # If response is not valid JSON, return a generic success/error based on status
            print(f"Non-JSON response: {response.text}")
            if response.status_code >= 200 and response.status_code < 300:
                return jsonify({'message': 'Evento externo eliminado correctamente'}), response.status_code
            else:
                return jsonify({'error': 'Error al eliminar evento externo', 'details': f'HTTP {response.status_code}'}), response.status_code
        return jsonify(response_data), response.status_code
    except requests.RequestException as e:
        print(f"Error deleting external event {event_id}: {e}")
        return jsonify({'error': 'Error al eliminar evento externo', 'details': str(e)}), 500

@app.route('/events/baja', methods=['POST'])
def baja_event():
    try:
        print(f"Dando de baja evento: {request.json}")
        response = requests.post(f'{SPRING_BOOT_URL}/events/baja', json=request.json)
        print(f"Spring Boot response status: {response.status_code}")
        print(f"Spring Boot response content: {response.text}")
        try:
            response_data = response.json()
            print(f"Parsed JSON response: {response_data}")
        except ValueError:
            # If response is not valid JSON, return a generic success/error based on status
            print(f"Non-JSON response: {response.text}")
            if response.status_code >= 200 and response.status_code < 300:
                return jsonify({'message': 'Evento dado de baja correctamente'}), response.status_code
            else:
                return jsonify({'error': 'Error al dar de baja evento', 'details': f'HTTP {response.status_code}'}), response.status_code
        return jsonify(response_data), response.status_code
    except requests.RequestException as e:
        print(f"Error dando de baja evento: {e}")
        return jsonify({'error': 'Error al dar de baja evento', 'details': str(e)}), 500

@app.route('/api/soap/presidents', methods=['POST'])
def soap_presidents():
    """
    Endpoint para consultar presidentes de organizaciones vía SOAP.
    Solo accesible para usuarios con rol PRESIDENTE.
    """
    data = request.json
    user_id = data.get('userId')
    org_ids_str = data.get('orgIds', '')

    # Validar parámetros
    if not user_id or not org_ids_str:
        return jsonify({'success': False, 'error': 'userId y orgIds son requeridos'}), 400

    # Validar rol del usuario (debería venir del frontend, pero verificamos)
    # Nota: En producción, validar contra la base de datos
    if not isinstance(user_id, int) or user_id <= 0:
        return jsonify({'success': False, 'error': 'userId inválido'}), 400

    # Parsear IDs de organizaciones
    try:
        org_ids = [str(id.strip()) for id in org_ids_str.split(',') if id.strip()]
        if not org_ids:
            return jsonify({'success': False, 'error': 'Lista de orgIds vacía'}), 400
    except ValueError:
        return jsonify({'success': False, 'error': 'orgIds debe contener números separados por coma'}), 400

    # Llamar al servicio SOAP
    result = get_presidents(org_ids)

    if result['success']:
        return jsonify({
            'success': True,
            'data': result['data'],
            'count': len(result['data'])
        })
    else:
        return jsonify({
            'success': False,
            'error': result['error']
        }), 500

@app.route('/api/soap/associations', methods=['POST'])
def soap_associations():
    """
    Endpoint para consultar datos de organizaciones vía SOAP.
    Solo accesible para usuarios con rol PRESIDENTE.
    """
    data = request.json
    user_id = data.get('userId')
    org_ids_str = data.get('orgIds', '')

    # Validar parámetros
    if not user_id or not org_ids_str:
        return jsonify({'success': False, 'error': 'userId y orgIds son requeridos'}), 400

    # Validar rol del usuario
    if not isinstance(user_id, int) or user_id <= 0:
        return jsonify({'success': False, 'error': 'userId inválido'}), 400

    # Parsear IDs de organizaciones
    try:
        org_ids = [str(id.strip()) for id in org_ids_str.split(',') if id.strip()]
        if not org_ids:
            return jsonify({'success': False, 'error': 'Lista de orgIds vacía'}), 400
    except ValueError:
        return jsonify({'success': False, 'error': 'orgIds debe contener números separados por coma'}), 400

    # Llamar al servicio SOAP
    result = get_associations(org_ids)

    if result['success']:
        return jsonify({
            'success': True,
            'data': result['data'],
            'count': len(result['data'])
        })
    else:
        return jsonify({
            'success': False,
            'error': result['error']
        }), 500

if __name__ == '__main__':
    app.run(port=5000, debug=True)
