package com.xpr.xpr.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class Cliente {
    public static String marshalToJson(User user) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(user);
    }

    public static String marshalToXml(User user) throws Exception {
        XmlMapper mapper = new XmlMapper();
        return mapper.writeValueAsString(user);
    }

    public static void main(String[] args) throws Exception {
        User user = new User();
        user.setName("Alice");
        user.setAge(30);
        user.setRole("Developer");

        // Enviar para o servidor, JSON ou XML
        String jsonData = marshalToJson(user);
        String xmlData = marshalToXml(user);

        // Código para enviar dados ao servidor via Socket (aqui é exemplo, depende de como será a implementação de envio)
    }
}
