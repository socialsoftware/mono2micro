package pt.ist.socialsoftware.mono2micro.utils.mojoCalculator.src.main.java;

import py4j.GatewayServer;

public class MoJoEntryPoint {

    private MoJo moJo;

    public MoJoEntryPoint() {
        moJo = new MoJo();
    }

    public double runMoJo() {
        /* These files were edited previously by python script */
        double v = moJo.executeMojo(
                new String[]{
                        "src/main/resources/" + "distrSrc.rsf",
                        "src/main/resources/" + "distrTarget.rsf",
                        "-fm"
                }
        );
        System.out.println("Executed Mojo. Result = " + v);
        return v;
    }

    public MoJo getMoJo() {
        return moJo;
    }


    public static void main(String[] args) {
        GatewayServer gatewayServer = new GatewayServer(new MoJoEntryPoint());
        gatewayServer.start();
        System.out.println("Gateway Server Started");
    }
}