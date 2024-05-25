package org.example;


import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import javax.swing.*;

public class ReceberSerial  {
    private String receber;
    private SerialPort serialPort = new SerialPort("COM3");
    private int posicaoVideoLooping = -1;
    private int posicaoVideoAtivar = -1;
    private int posicaoVideoAtivar2 = -1;
    private double tempoVideo;
    private double tempoVideo2;
    private double tempoVideoRestante;
    private String comandoVideoLooping = "C:\\Temp\\VideoLooping_3Videos.mp4";
    private String comandoVideoAtivar = "C:\\Temp\\VideoAtivar_3Videos.mp4";
    private String comandoVideoAtivar2 = "C:\\Temp\\VideoAtivar2_3Videos.mp4";
    private Integer contadorPortaCOM = 0;
    private Integer tentativaReconexao = 0;

    public boolean iniciaReceberSerial(){
        try {
            //Abre a comunicação com a porta COM3 do Arduino
            serialPort.openPort();
            serialPort.setParams(9600, 8, 1, 0);
            // serialPort.writeBytes("Hello World!!!".getBytes());
            serialPort.setRTS(true);

            //Verifica quantos segundos o video que será diparado quando sensor for ativado
            tempoVideo = verificarMinuto("C:/Temp/VideoAtivar_3Videos.mp4");
            tempoVideo2 = verificarMinuto("C:/Temp/VideoAtivar2_3Videos.mp4");

            try {
                //Entra em Looping verificando oque está sendo recebido
                while(true) {
                    //Seta Contador de portas COM
                    contadorPortaCOM = 0;

                    //Verifica se existe alguma Mensagem
                    receber = serialPort.readString();
                    String[] portNames = SerialPortList.getPortNames();

                    //Verifica se a porta COM3 está ativa
                    for (int i = 0; i < portNames.length; i++){
                        if(portNames[i].equals("COM3")){
                            contadorPortaCOM = contadorPortaCOM + 1;
                            break;
                        }
                    }

                    //Caso a porta COM3 não esteja ativa
                    if( contadorPortaCOM == 0){

                        try {
                            // System.setProperty("java.awt.headless", "false");
                            //JOptionPane.showMessageDialog(null, "Perda de conexão, tentando se reconectar");

                            //Aguarda 5sec para tentar se reconectar
                            Thread.sleep(5000);

                            //tenta fazer a reconexão ()
                            serialPort.openPort();
                            serialPort.setParams(9600, 8, 1, 0);
                            serialPort.setRTS(true);

                            //Exive msg no CMD
                            System.out.println("Perda de conexão, tentando se reconectar");
                        }catch (Exception ex){
                            ex.printStackTrace();

                            //Caso erro atualiza tentativas de reconexão
                            tentativaReconexao = tentativaReconexao + 1;

                            if (tentativaReconexao == 4){
                                break;
                            }
                        }
                    }else{
                        //Caso a porta COM3 teve aglum problema de conexão, a conexão é fechada e aberta novamente
                        if(tentativaReconexao != 0) {
                            //Seta para 0 as tentativas de conexão
                            tentativaReconexao = 0;

                            serialPort.setRTS(false);
                            serialPort.closePort();

                            serialPort.openPort();
                            serialPort.setParams(9600, 8, 1, 0);
                            serialPort.setRTS(true);
                        }
                    }

                    //Se tiver alguma mensagem olha se foi recibido 0 ou 1
                    if (receber != null) {
                        posicaoVideoLooping = receber.indexOf("0");
                        posicaoVideoAtivar = receber.indexOf("1");
                        posicaoVideoAtivar2 = receber.indexOf("2");
                    }

                    //Caso recebido 1, será ativado o video do sensor
                    if (posicaoVideoAtivar != -1) {
                        //Envia 1 como resposta para o arduino e Executa o video do sensor
                        System.out.println(posicaoVideoAtivar + "/ VideoAtivar");
                        serialPort.writeInt(1);
                        Runtime.getRuntime().exec("rundll32 SHELL32.DLL,ShellExec_RunDLL " +comandoVideoAtivar);

                        //Seta variaveis
                        posicaoVideoLooping = -1;
                        posicaoVideoAtivar = -1;
                        tempoVideoRestante = tempoVideo;

                        //Entrara em Looping até o video do sensor for finalizado OU até rece 0 do Arduino
                        while(tempoVideoRestante > 0) {
                            //A cada meio segundo verifica se recebo 0 do Arduino
                            Thread.sleep(500);
                            receber = serialPort.readString();
                            contadorPortaCOM = 0;

                            String[] portNames2 = SerialPortList.getPortNames();

                            //Verifica se a porta COM3 está ativa
                            for (int i = 0; i < portNames2.length; i++){
                                if(portNames2[i].equals("COM3")){
                                    contadorPortaCOM = contadorPortaCOM + 1;
                                    break;
                                }
                            }
                            if( contadorPortaCOM == 0) {
                                break;
                            }

                            //Se tiver alguma mensagem olha se foi recibido 0
                            if (receber != null) {
                                //Se for recebido 0, saira do Looping e executada o primeiro video padrão
                                posicaoVideoLooping = receber.indexOf("0");
                                if (posicaoVideoLooping != -1) {
                                    break;
                                }
                            }

                            //Seta variavel
                            tempoVideoRestante = tempoVideoRestante - 0.5;
                        }

                        //Envia 0 como resposta para o arduino e Executa o video padrão
                        System.out.println(posicaoVideoLooping + "VideoLooping");
                        serialPort.writeInt(0);
                        Runtime.getRuntime().exec("rundll32 SHELL32.DLL,ShellExec_RunDLL " +comandoVideoLooping);
                        posicaoVideoLooping = -1;
                    }

                    if (posicaoVideoAtivar2 != -1) {
                        //Envia 1 como resposta para o arduino e Executa o video do sensor
                        System.out.println(posicaoVideoAtivar2 + "/ VideoAtivar2");
                        serialPort.writeInt(2);
                        Runtime.getRuntime().exec("rundll32 SHELL32.DLL,ShellExec_RunDLL " +comandoVideoAtivar2);

                        //Seta variaveis
                        posicaoVideoLooping = -1;
                        posicaoVideoAtivar2 = -1;
                        tempoVideoRestante = tempoVideo2;

                        //Entrara em Looping até o video do sensor for finalizado OU até rece 0 do Arduino
                        while(tempoVideoRestante > 0) {
                            //A cada meio segundo verifica se recebo 0 do Arduino
                            Thread.sleep(500);
                            receber = serialPort.readString();
                            contadorPortaCOM = 0;

                            String[] portNames2 = SerialPortList.getPortNames();

                            //Verifica se a porta COM3 está ativa
                            for (int i = 0; i < portNames2.length; i++){
                                if(portNames2[i].equals("COM3")){
                                    contadorPortaCOM = contadorPortaCOM + 1;
                                    break;
                                }
                            }
                            if( contadorPortaCOM == 0) {
                                break;
                            }

                            //Se tiver alguma mensagem olha se foi recibido 0
                            if (receber != null) {
                                //Se for recebido 0, saira do Looping e executada o primeiro video padrão
                                posicaoVideoLooping = receber.indexOf("0");
                                if (posicaoVideoLooping != -1) {
                                    break;
                                }

                            }

                            //Seta variavel
                            tempoVideoRestante = tempoVideoRestante - 0.5;
                        }

                        //Envia 0 como resposta para o arduino e Executa o video padrão
                        System.out.println(posicaoVideoLooping + "VideoLooping");
                        serialPort.writeInt(0);
                        Runtime.getRuntime().exec("rundll32 SHELL32.DLL,ShellExec_RunDLL " +comandoVideoLooping);
                        posicaoVideoLooping = -1;
                    }

                    //Caso recebido 0, será ativado o video do sensor
                    if (posicaoVideoLooping != -1) {
                        //Envia 0 como resposta para o arduino e Executa o video padrão
                        System.out.println(posicaoVideoLooping + "VideoLooping");
                        serialPort.writeInt(0);
                        Runtime.getRuntime().exec("rundll32 SHELL32.DLL,ShellExec_RunDLL " + comandoVideoLooping);
                    }

                    //Seta variavel
                    posicaoVideoLooping = -1;
                    posicaoVideoAtivar = -1;
                    posicaoVideoAtivar2 = -1;
                }
            } catch (Exception ex) {
                ex.printStackTrace();

                //Caso aconteceça algum erro no processamento, será fechado as portas
                serialPort.setRTS(false);
                serialPort.closePort();
                return true;
            }

        }
        catch (SerialPortException ex){
            try {
                //Caso aconteceça algum erro no processamento, será fechado as portas
                serialPort.setRTS(false);
                serialPort.closePort();
            }catch (Exception e){
                //Em caso de erro ao tentar se conectar ao Arduino, retornada msg em tela.
                System.out.println(ex);
                System.setProperty("java.awt.headless", "false");
                JOptionPane.showMessageDialog(null, "Erro ao tentar se conectar: "+ex);
                return false;
            }

        }
        return true;
    }


    public double verificarMinuto(String caminhoVideo){
        String videoFilePath = caminhoVideo;
        double durationInSeconds = 0;

        try {
            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoFilePath);
            grabber.start();

            // Obtenha a duração do vídeo em segundos
            durationInSeconds = grabber.getLengthInTime() / 1000000.0;

            System.out.println("Duração do vídeo: " + durationInSeconds + " segundos");

            grabber.stop();

            return durationInSeconds;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return durationInSeconds;
    }
}
