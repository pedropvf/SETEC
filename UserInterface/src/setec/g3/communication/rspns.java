/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package setec.g3.communication;

import java.io.Serializable;

/**
 *
 * @author Luis Ungaro
 */

/*These class must be Serializable!*/
public class rspns implements Serializable {
    public final byte id;
    
    public rspns(byte id){
        this.id = id;
    }
}

