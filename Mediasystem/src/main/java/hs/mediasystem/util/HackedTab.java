package hs.mediasystem.util;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import com.sun.javafx.scene.traversal.Direction;
import com.sun.javafx.scene.traversal.TraversalEngine;
import com.sun.javafx.scene.traversal.TraverseListener;

/**
 * Use this as the root of a Tab to be able to navigate away from the Tab using the cursor keys.
 */
public class HackedTab extends StackPane {

  @SuppressWarnings("deprecation")
  public HackedTab(Node content) {
    getChildren().add(content);

    TraversalEngine traversalEngine = new TraversalEngine(this, false) {
      private int traverseOccurances;

      {
        addTraverseListener(new TraverseListener() {
          @Override
          public void onTraverse(Node node, Bounds bounds) {
            traverseOccurances++;
          }
        });
      }

      @Override
      public void trav(Node node, Direction direction) {
        int currentTraverseOccurances = traverseOccurances;

        super.trav(node, direction);

        if(currentTraverseOccurances == traverseOccurances) {
          // Nothing happened, try and escape this context
          if(direction == Direction.LEFT || direction == Direction.UP) {
            super.trav(node, Direction.PREVIOUS);
          }
          else {
            super.trav(node, Direction.NEXT);
          }
        }
      }
    };

    impl_traversalEngineProperty().set(traversalEngine);
  }
}