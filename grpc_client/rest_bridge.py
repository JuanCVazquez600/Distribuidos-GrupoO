from flask import Flask, jsonify, request
from flask_cors import CORS
import grpc
from proto import usuarios_pb2, usuarios_pb2_grpc, eventos_pb2, eventos_pb2_grpc, inventario_pb2, inventario_pb2_grpc

app = Flask(__name__)
CORS(app)

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

if __name__ == '__main__':
    app.run(port=5000, debug=True)
