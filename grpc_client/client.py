import grpc
from proto import usuarios_pb2, usuarios_pb2_grpc

def run():
    channel = grpc.insecure_channel('localhost:9090')
    stub = usuarios_pb2_grpc.UsuarioServiceStub(channel)

    request = usuarios_pb2.LoginRequest(
        usuarioEmail="usuario1",
        clave="clave123"
    )
    try:
        response = stub.Login(request)
        if response.exito:
            print("Login exitoso:", response.mensaje)
            print("Usuario:", response.usuario.nombreUsuario)
        else:
            print("Login fallido:", response.mensaje)
    except grpc.RpcError as e:
        print(f"Error en llamada gRPC: {e.details()}")

def grpc_listar_usuarios():
    channel = grpc.insecure_channel('localhost:9090')
    stub = usuarios_pb2_grpc.UsuarioServiceStub(channel)
    try:
        response = stub.ListarUsuarios(usuarios_pb2.Empty())
        print(f"Usuarios obtenidos: {len(response.usuarios)}")
        for u in response.usuarios:
            print(f"ID: {u.id}, NombreUsuario: {u.nombreUsuario}, Nombre: {u.nombre}, Email: {u.email}")
    except grpc.RpcError as e:
        print(f"Error en llamada ListarUsuarios: {e.details()}")

if __name__ == '__main__':
    run()
    grpc_listar_usuarios()
