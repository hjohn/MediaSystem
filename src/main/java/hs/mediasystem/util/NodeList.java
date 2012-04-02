package hs.mediasystem.util;

public class NodeList<T> {
  private Node head;

  public synchronized Node addHead(T content) {
    head = new Node(content, head, null);

    if(head.next != null) {
      head.next.previous = head;
    }

    return head;
  }

  public synchronized T removeHead() {
    return unlink(head);
  }

  public synchronized T unlink(Node node) {
    if(node == null) {
      return null;
    }

    if(node.previous != null) {
      node.previous.next = node.next;
    }
    if(node.next != null) {
      node.next.previous = node.previous;
    }
    if(node.equals(head)) {
      head = node.next;
    }

    return node.content;
  }

  public final class Node {
    private final T content;

    private Node next;
    private Node previous;

    Node(T content, Node next, Node previous) {
      this.content = content;
      this.next = next;
      this.previous = previous;
    }
  }
}