/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Mapa;

import Geoconhecimento.Geoconhecimento;
import Geoconhecimento.TableModel;
import Mapa.Arco;
import Mapa.Figura;
import static Mapa.Figura.*;
import Mapa.Ponto;
import Prolog.Parser;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Label;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JPanel;

/**
 *
 * @author Chalkos
 */
public class Mapa {
    protected static final Color normal = new Color(0x000000);
    protected static final Color normalLinhas = new Color(0x666666);
    protected static final Color selected = new Color(0x0033FF);
    protected static final Color path = new Color(0xFF6600);
    protected static final Color destination = new Color(0xFF0000);

    private ArrayList<Figura> shapes = new ArrayList<>();
    private JPanel panel;
    private Graphics g;

    public final Etiqueta activeLabel = new Etiqueta();

    public Mapa(JPanel panel, ArrayList<Ponto> pontos, ArrayList<Arco> arcos) {
        this.panel = panel;
        this.g = panel.getGraphics();
        
        shapes.addAll(arcos);
        shapes.addAll(pontos);

        shapes.add(activeLabel);
    }

    public double mouseXtoMapX(int mouseX) {
        return ((mouseX - meioX) / zoom - offsetX);
    }

    public double mouseYtoMapY(int mouseY) {
        return ((mouseY - meioY) / zoom - offsetY);
    }

    public void desenharTudo() {
        this.g = panel.getGraphics();
        g.clearRect(0, 0, panel.getWidth(), panel.getHeight());
        
        /*
        // marcar o meio
        g.setColor(new Color(0xFF0000));
        g.drawLine(0, panel.getHeight() / 2, panel.getWidth(), panel.getHeight() / 2);
        g.drawLine(panel.getWidth() / 2, 0, panel.getWidth() / 2, panel.getHeight());
        */
        
        Figura.setMeio(panel.getWidth() / 2, panel.getHeight() / 2);
        for (Figura s : shapes) {
            if (s != null) {
                s.desenhar(g);
            }
        }
    }

    public void showLabel(int mouseX, int mouseY) {

        double x = mouseXtoMapX(mouseX);
        double y = mouseYtoMapY(mouseY);
        //System.out.println(shapes.size());
        for (Figura s : shapes) {
            if (s != null && s.getClass() == Ponto.class) {
                Ponto p = (Ponto) s;

                
                if (p.getDiametro() / 2.0 >= Math.sqrt(Math.pow(p.getCenterX() - x, 2) + Math.pow(p.getCenterY() - y, 2))) {
                    //System.out.println("intersect!!!! diam=" + p.getDiametro() + " x=" + x + " y=" + y);
                    
                    activeLabel.setPosition(mouseX, mouseY);
                    activeLabel.setText(p.getNome());
                    activeLabel.activate();
                    desenharTudo();
                    //activeLabel.desenhar(g);
                    activeLabel.deactivate();
                    return;
                }
            }
        }
        desenharTudo();
    }

    public void updateTable(Geoconhecimento gc, TableModel model, int mouseX, int mouseY) {

        double x = mouseXtoMapX(mouseX);
        double y = mouseYtoMapY(mouseY);
        //System.out.println(shapes.size());
        for (Figura s : shapes) {
            if (s != null && s.getClass() == Ponto.class) {
                Ponto p = (Ponto) s;

                
                if (p.getDiametro() / 2.0 >= Math.sqrt(Math.pow(p.getCenterX() - x, 2) + Math.pow(p.getCenterY() - y, 2))) {
                    //System.out.println("intersect!!!! diam=" + p.getDiametro() + " x=" + x + " y=" + y);
                    
                    
                    clearAll();
                    p.color = selected;
                    
                    desenharTudo();
                    
                    gc.actualizarPropriedades(p);
                    model.setDados(p.getNome(), p.getPropriedades());
                    
                    return;
                }
            }
        }
    }
    
    private void clearSelected(){
        for (Figura s : shapes) {
            if (s != null && s.getClass() == Ponto.class) {
                Ponto p = (Ponto) s;
                
                p.color = normal;
            }
        }
    }
    
    private void clearPath(){
        for (Figura s : shapes) {
            if (s != null && s.getClass() == Ponto.class) {
                Ponto p = (Ponto) s;
                if( p.color != Mapa.selected )
                    p.color = normal;
            }
            if (s != null && s.getClass() == Arco.class) {
                Arco a = (Arco) s;
                a.color = Mapa.normalLinhas;
            }
        }
    }
    
    private void clearAll(){
        for (Figura s : shapes) {
            if (s != null && s.getClass() == Ponto.class) {
                Ponto p = (Ponto) s;
                p.color = normal;
            }
            if (s != null && s.getClass() == Arco.class) {
                Arco a = (Arco) s;
                a.color = Mapa.normalLinhas;
            }
        }
    }

    public void caminhoMaisCurto(Geoconhecimento gc, TableModel model, int mouseX, int mouseY) {
        Ponto origem = null;
        for (Figura s : shapes) {
            if (s != null && s.getClass() == Ponto.class) {
                Ponto p = (Ponto) s;
                
                if(p.color == selected){
                    origem = p;
                    break;
                }
            }
        }
        
        if( origem != null){
            double x = mouseXtoMapX(mouseX);
            double y = mouseYtoMapY(mouseY);
            //System.out.println(shapes.size());
            for (Figura s : shapes) {
                if (s != null && s.getClass() == Ponto.class) {
                    Ponto destino = (Ponto) s;


                    if (destino.getDiametro() / 2.0 >= Math.sqrt(Math.pow(destino.getCenterX() - x, 2) + Math.pow(destino.getCenterY() - y, 2))) {
                        //System.out.println("intersect!!!! diam=" + p.getDiametro() + " x=" + x + " y=" + y);

                        
                        // limpar cores
                        clearPath();
                        destino.color = destination;
                        // obter o caminhho mais curto
                        ArrayList<String> nomes = gc.pontosDoCaminhoMaisCurto(origem, destino);
                        // colorir os arcos
                        colorirCaminho(nomes);
                        // obter as distancias e o total
                        ArrayList<Double> distancias = gc.distanciasDoCaminhoMaisCurto(origem, destino);
                        // preencher a tabela
                        model.setCaminho(nomes, distancias);
                        
                        colorirCaminho(nomes);
                        
                        desenharTudo();

                        return;
                    }
                }
            }
        }else
            updateTable(gc, model, mouseX, mouseY);
    }
    
    private void colorirCaminho(ArrayList<String> nomes){
        // encontrar os pontos envolvidos
        ArrayList<Ponto> pontos = new ArrayList<Ponto>();
        for (Figura s : shapes) {
            if (s != null && s.getClass() == Ponto.class) {
                Ponto p = (Ponto) s;
                
                for (String nome : nomes)
                    if (nome.equals(p.getNome()))
                        pontos.add(p);
            }
        }
        
        // encontrar os arcos envolvidos
        ArrayList<Arco> arcos = new ArrayList<>();
        for (Figura s : shapes) {
            if (s != null && s.getClass() == Arco.class) {
                Arco a = (Arco) s;
                
                for(Ponto origem : pontos){
                    for(Ponto destino : pontos){
                        if( a.x1 == (int)origem.originalX &&
                            a.y1 == (int)origem.originalY &&
                            a.x2 == (int)destino.originalX &&
                            a.y2 == (int)destino.originalY){
                                arcos.add(a);
                        }
                    }
                }
            }
        }
        
        //pintar os arcos
        for(Arco a : arcos){
            a.color = Mapa.path;
        }
        
        //pintar os pontos que estao a normal
        for(Ponto p : pontos){
            if( p.color == Mapa.normal )
                p.color = Mapa.path;
        }
    }

    public void pontosNaArea(Geoconhecimento gc, TableModel model, int x1, int y1, int x2, int y2) {
        ArrayList<String> ids = gc.nomePontosNaArea(x1, y1, x2, y2);
        
        HashMap<String,String> tabela = new HashMap<>();
        
        for(String id : ids){
            for (Figura s : shapes) {
                if (s != null && s.getClass() == Ponto.class) {
                    Ponto p = (Ponto) s;
                    
                    if( id.equals(p.getNome()) ){
                        gc.actualizarPropriedades(p);
                        tabela.put(id, p.getLocal());
                    }
                }
            }
        }
        
        model.setEncontrados(tabela);
    }
}
