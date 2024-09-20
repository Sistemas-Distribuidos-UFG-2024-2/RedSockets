import socket

def start_server():


    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.bind(('localhost', 8081))
    server_socket.listen(1)

    print("Servidor aguardando conexões...")


    conn, addr = server_socket.accept()
    print(f"Cliente conectado: {addr}")

    # Recebe e remove espaços/brancos extras como \n ou \r
    data = conn.recv(1024).decode().strip()  
    print(f"Mensagem-cliente: {data}")


    if data == "Hello":
        conn.sendall("World\n".encode())  
        print("Mensagem-servidor: World")

    conn.close()

if __name__ == "__main__":
    start_server()
