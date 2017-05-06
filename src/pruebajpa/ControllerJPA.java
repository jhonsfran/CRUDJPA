/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pruebajpa;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.swing.JOptionPane;
import view.CrudPersonal;
import view.ReporteFindAll;

/**
 *
 * @author invitado
 */
public class ControllerJPA implements ActionListener{
    
    CrudPersonal vistaCrud;
    ReporteFindAll reporteFindAll;
    private static EntityManagerFactory emf = null;

    public ControllerJPA() {
        
        if(emf == null){
            emf = Persistence.createEntityManagerFactory("PruebaJPAPU");
        }
        
        vistaCrud = new CrudPersonal();
        
        TipoDocumentoJpaController tpdoc = new TipoDocumentoJpaController(emf);
        vistaCrud.cargarDocumentos(tpdoc.findTipoDocumentoEntities());
        vistaCrud.agregarEscuchas(this);
        vistaCrud.setVisible(true);
        
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == this.vistaCrud.getInsert()) {
            //JOptionPane.showMessageDialog(null, "lo escucho");
            
            if(!vistaCrud.validarDatos()){
                JOptionPane.showMessageDialog(null, "No se realizó la inserción");
            }else{
                PersonalJpaController dao = new PersonalJpaController(emf);//Creamos un controlador de personal
                Personal persona = new Personal();//Creamos un objeto personal
                persona.setIdentificacionPersonal(vistaCrud.getId_doc().getText());
                persona.setNombre(vistaCrud.getNombre().getText());
                persona.setApellido(vistaCrud.getApellido().getText());
                
                TipoDocumentoJpaController tpdoc = new TipoDocumentoJpaController(emf);//Debido a que Tipo documento es una Foreing Key en Personal, debemos instanciar un controlador
                //de tipo TipoDocumento
                persona.setTpDocumento(tpdoc.findTipoDocumento(vistaCrud.getTp_doc().getSelectedIndex()+1));//Buscamos el tipo documento con primary key = 1 (C.C.) y se lo pasamos al objeto personal
                
                try {
                    dao.create(persona);
                    vistaCrud.setCampos();
                    JOptionPane.showMessageDialog(null, "Datos ingresados exitosamente");
                    
                } catch (Exception ex) {
                    //Logger.getLogger(ControllerJPA.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(null,ex.getCause().getMessage());
                    vistaCrud.setCampos();
                }
        
            }
        }
        
        if (e.getSource() == this.vistaCrud.getFind()) {
            //JOptionPane.showMessageDialog(null, "lo escucho");

            if (vistaCrud.getId_doc().getText().equals("")) {
                JOptionPane.showMessageDialog(null, "Falló la consulta. Debe ingresar un Documento de identidad");
            } else {
                
                PersonalJpaController dao = new PersonalJpaController(emf);//Creamos un controlador de personal

                Personal persona = new Personal();//Creamos un objeto personal
                
                try {
                    
                    persona = dao.findPersonal(vistaCrud.getId_doc().getText());//lo igualamos al objeto que encuentra en la base de datos
                    vistaCrud.getNombre().setText(persona.getNombre());
                    vistaCrud.getApellido().setText(persona.getApellido());
                    vistaCrud.getTp_doc().setSelectedItem(persona.getTpDocumento().getTpCodigo());
                    
                    vistaCrud.getId_doc().setEnabled(false);//El número de identidad no se puede modificar
                    vistaCrud.getFind().setEnabled(false);//No se pueden volver a buscar el mismo registro que se ha buscado
                    vistaCrud.getInsert().setEnabled(false);//Tampoco se puede volver a ingresar

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "No se encontraron resultados en la base de datos");
                    vistaCrud.setCampos();//Si se lanza una excepción lo mejor es borrar todos los campos
                }

                

            }
        }
        
        
        if (e.getSource() == this.vistaCrud.getUpdate()) {

            if (!vistaCrud.validarDatos()) {
                JOptionPane.showMessageDialog(null, "Falló la actualización");
            } else {

                vistaCrud.getFind().setEnabled(true);//Se habilitan los botones que puedan estar deshabilitados
                vistaCrud.getInsert().setEnabled(true);

                PersonalJpaController dao = new PersonalJpaController(emf);//Creamos un controlador de personal
                Personal persona = new Personal();//Creamos un objeto personal
                persona.setIdentificacionPersonal(vistaCrud.getId_doc().getText());
                persona.setNombre(vistaCrud.getNombre().getText());
                persona.setApellido(vistaCrud.getApellido().getText());
                
                TipoDocumentoJpaController tpdoc = new TipoDocumentoJpaController(emf);
                persona.setTpDocumento(tpdoc.findTipoDocumento(vistaCrud.getTp_doc().getSelectedIndex()+1));
                
                try {

                    dao.edit(persona);
                    JOptionPane.showMessageDialog(null, "Datos actualizados exitosamente");

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "No se encontraron resultados en la base de datos");
                }
                
                
                vistaCrud.getId_doc().setEnabled(true);//al final se debe habilitar el campo de documento
                vistaCrud.setCampos();

            }
        }
        
        
        if (e.getSource() == this.vistaCrud.getDelete()) {

            if (vistaCrud.getId_doc().getText().equals("")) {
                JOptionPane.showMessageDialog(null, "Falló de eliminación. Debe ingresar un Documento de identidad");
            } else {

                vistaCrud.getFind().setEnabled(true);
                vistaCrud.getInsert().setEnabled(true);
                
                PersonalJpaController dao = new PersonalJpaController(emf);//Creamos un controlador de personal
                
                try {
                    
                    dao.destroy(vistaCrud.getId_doc().getText());
                    JOptionPane.showMessageDialog(null, "Datos Eliminados con éxito!");

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "No se encontraron resultados en la base de datos para ese numero de identidad");
                }
                
                vistaCrud.getId_doc().setEnabled(true);
                vistaCrud.setCampos();

                

            }
        }
        
        
        if (e.getSource() == this.vistaCrud.getFindAll()) {

            PersonalJpaController dao = new PersonalJpaController(emf);//Creamos un controlador de personal

            reporteFindAll = new ReporteFindAll();
            
            Personal mi_personita;
            List<Personal> personas = dao.findPersonalEntities();
            Iterator iterator = personas.iterator();
            
            while(iterator.hasNext()){

                mi_personita = (Personal) iterator.next();
                reporteFindAll.agregaFilaReporte(mi_personita);

            }
            
            reporteFindAll.setVisible(true);
            
        }
        
    }

}
