package ch.unifr.diva.divaservices.communicator.returnTypes;

import java.util.Iterator;
import java.util.List;

import static javafx.scene.input.KeyCode.T;

/**
 * @author Marcel Würsch
 *         marcel.wuersch@unifr.ch
 *         http://diuf.unifr.ch/main/diva/home/people/marcel-w%C3%BCrsch
 *         Created on: 08.10.2015.
 */
public abstract class AbstractHighlighter<T> implements Iterable<T> {

     protected List<T> data;

     public AbstractHighlighter(List<T> data){
          this.data = data;
     }

     public List getData(){
          return data;
     }

     @Override
     public Iterator<T> iterator(){
          return data.iterator();
     }
}
