#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <pthread.h>

#define PORTA 8080

// Função para verificar se o cliente pode se aposentar
void* processar_cliente(void* socket_cliente) {
    int novo_socket = *(int*)socket_cliente;
    int idade, tempo_trabalho;
    char resposta[1024] = {0};

    // Lendo as variáveis idade e tempo de trabalho do cliente
    read(novo_socket, &idade, sizeof(idade));
    read(novo_socket, &tempo_trabalho, sizeof(tempo_trabalho));

    printf("Idade recebida: %d\n", idade);
    printf("Tempo de trabalho recebido: %d\n", tempo_trabalho);

    // Verificando se pode se aposentar
    if (idade >= 65 || tempo_trabalho >= 30 || (idade >= 60 && tempo_trabalho >= 25)) {
        strcpy(resposta, "Pode se aposentar");
    } else {
        // Não pode se aposentar, calcular o que falta
        int falta_idade = 65 - idade;
        int falta_tempo = 30 - tempo_trabalho;

        if (idade < 60 || tempo_trabalho < 25) {
            sprintf(resposta, "Nao pode se aposentar. Faltam %d anos de idade ou %d anos de trabalho para a aposentadoria.",
                    falta_idade > 0 ? falta_idade : 0, falta_tempo > 0 ? falta_tempo : 0);
        } else if (idade < 65) {
            sprintf(resposta, "Nao pode se aposentar. Faltam %d anos de idade.", falta_idade);
        } else if (tempo_trabalho < 30) {
            sprintf(resposta, "Nao pode se aposentar. Faltam %d anos de trabalho.", falta_tempo);
        }
    }

    // Enviando a resposta para o cliente
    send(novo_socket, resposta, strlen(resposta), 0);
    printf("Resposta enviada: %s\n", resposta);

    // Fechando o socket
    close(novo_socket);
    free(socket_cliente);  // Liberar memória alocada
    pthread_exit(NULL);    // Finalizar a thread
}

int main() {
    int servidor_fd, novo_socket;
    struct sockaddr_in endereco;
    int addrlen = sizeof(endereco);

    // Criando o socket
    if ((servidor_fd = socket(AF_INET, SOCK_STREAM, 0)) == 0) {
        perror("Falha ao criar o socket");
        exit(EXIT_FAILURE);
    }

    // Configurando o endereço do servidor
    endereco.sin_family = AF_INET;
    endereco.sin_addr.s_addr = INADDR_ANY;
    endereco.sin_port = htons(PORTA);

    // Associando o socket ao endereço e porta
    if (bind(servidor_fd, (struct sockaddr *)&endereco, sizeof(endereco)) < 0) {
        perror("Falha ao associar o socket");
        exit(EXIT_FAILURE);
    }

    // Escutando por conexões
    if (listen(servidor_fd, 10) < 0) {  // Permitir até 10 conexões pendentes
        perror("Erro em listen");
        exit(EXIT_FAILURE);
    }
    printf("Servidor em execução na porta %d. Aguardando conexões...\n", PORTA);

    // Loop infinito para aceitar conexões de clientes
    while (1) {
        if ((novo_socket = accept(servidor_fd, (struct sockaddr *)&endereco, (socklen_t*)&addrlen)) < 0) {
            perror("Erro em accept");
            exit(EXIT_FAILURE);
        }

        printf("Nova conexão recebida!\n");

        // Criar um ponteiro para o socket para passar para a thread
        int* socket_cliente = malloc(sizeof(int));
        *socket_cliente = novo_socket;

        // Criando uma nova thread para processar o cliente
        pthread_t thread_id;
        if (pthread_create(&thread_id, NULL, processar_cliente, socket_cliente) != 0) {
            perror("Erro ao criar a thread");
            free(socket_cliente);  // Liberar memória alocada em caso de erro
        }

        // Desvincular a thread para que seja limpa automaticamente após terminar
        pthread_detach(thread_id);
    }

    // Fechando o socket do servidor
    close(servidor_fd);

    return 0;
}
