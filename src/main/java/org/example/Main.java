package org.example;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        boolean resposta;
        Integer contador = 0;

        String comando = "C:\\Temp\\VideoLooping_3Videos.mp4";
        Runtime.getRuntime().exec("rundll32 SHELL32.DLL,ShellExec_RunDLL " + comando);

        ReceberSerial Serial = new ReceberSerial();

        while (true) {
            resposta = Serial.iniciaReceberSerial();

            //Vai tentar se conectar 3x, se não consguir fechara a aplicação
            if(resposta == false){
                contador = contador + 1;
                if(contador >= 3) {
                    break;
                }
            }else{
                contador = 0;
            }

        }
    }
}