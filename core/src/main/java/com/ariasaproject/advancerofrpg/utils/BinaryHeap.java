package com.ariasaproject.advancerofrpg.utils;

import java.util.Arrays;

public class BinaryHeap<T extends BinaryHeap.Node> {
    private final boolean isMaxHeap;
    public int size;
    private Node[] nodes;

    public BinaryHeap() {
        this(16, false);
    }

    public BinaryHeap(int capacity, boolean isMaxHeap) {
        this.isMaxHeap = isMaxHeap;
        nodes = new Node[capacity];
    }

    /**
     * Adds the node to the heap using its current value. The node should not
     * already be in the heap.
     */
    public T add(T node) {
        // Expand if necessary.
        if (size == nodes.length) {
            Node[] newNodes = new Node[size << 1];
            System.arraycopy(nodes, 0, newNodes, 0, size);
            nodes = newNodes;
        }
        // Insert at end and bubble up.
        node.index = size;
        nodes[size] = node;
        up(size++);
        return node;
    }

    /**
     * Sets the node's value and adds it to the heap. The node should not already be
     * in the heap.
     */
    public T add(T node, float value) {
        node.value = value;
        return add(node);
    }

    /**
     * Returns true if the heap contains the specified node.
     *
     * @param identity If true, == comparison will be used. If false, .equals()
     *                 comparison will be used.
     */
    public boolean contains(T node, boolean identity) {
        if (node == null)
            throw new IllegalArgumentException("node cannot be null.");
        if (identity) {
            for (Node n : nodes)
                if (n == node)
                    return true;
        } else {
            for (Node other : nodes)
                if (other.equals(node))
                    return true;
        }
        return false;
    }

    /**
     * Returns the first item in the heap. This is the item with the lowest value
     * (or highest value if this heap is configured as a max heap).
     */
    public T peek() {
        if (size == 0)
            throw new IllegalStateException("The heap is empty.");
        return (T) nodes[0];
    }

    /**
     * Removes the first item in the heap and returns it. This is the item with the
     * lowest value (or highest value if this heap is configured as a max heap).
     */
    public T pop() {
        Node removed = nodes[0];
        if (--size > 0) {
            nodes[0] = nodes[size];
            nodes[size] = null;
            down(0);
        } else
            nodes[0] = null;
        return (T) removed;
    }

    public T remove(T node) {
        if (--size > 0) {
            Node moved = nodes[size];
            nodes[size] = null;
            nodes[node.index] = moved;
            if (moved.value < node.value ^ isMaxHeap)
                up(node.index);
            else
                down(node.index);
        } else
            nodes[0] = null;
        return node;
    }

    /**
     * Returns true if the heap has one or more items.
     */
    public boolean notEmpty() {
        return size > 0;
    }

    /**
     * Returns true if the heap is empty.
     */
    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        Arrays.fill(nodes, 0, size, null);
        size = 0;
    }

    /**
     * Changes the value of the node, which should already be in the heap.
     */
    public void setValue(T node, float value) {
        float oldValue = node.value;
        node.value = value;
        if (value < oldValue ^ isMaxHeap)
            up(node.index);
        else
            down(node.index);
    }

    private void up(int index) {
        Node[] nodes = this.nodes;
        Node node = nodes[index];
        float value = node.value;
        while (index > 0) {
            int parentIndex = (index - 1) >> 1;
            Node parent = nodes[parentIndex];
            if (value < parent.value ^ isMaxHeap) {
                nodes[index] = parent;
                parent.index = index;
                index = parentIndex;
            } else
                break;
        }
        nodes[index] = node;
        node.index = index;
    }

    private void down(int index) {
        Node[] nodes = this.nodes;
        int size = this.size;
        Node node = nodes[index];
        float value = node.value;
        while (true) {
            int leftIndex = 1 + (index << 1);
            if (leftIndex >= size)
                break;
            int rightIndex = leftIndex + 1;
            // Always has a left child.
            Node leftNode = nodes[leftIndex];
            float leftValue = leftNode.value;
            // May have a right child.
            Node rightNode;
            float rightValue;
            if (rightIndex >= size) {
                rightNode = null;
                rightValue = isMaxHeap ? -Float.MAX_VALUE : Float.MAX_VALUE;
            } else {
                rightNode = nodes[rightIndex];
                rightValue = rightNode.value;
            }
            // The smallest of the three values is the parent.
            if (leftValue < rightValue ^ isMaxHeap) {
                if (leftValue == value || (leftValue > value ^ isMaxHeap))
                    break;
                nodes[index] = leftNode;
                leftNode.index = index;
                index = leftIndex;
            } else {
                if (rightValue == value || (rightValue > value ^ isMaxHeap))
                    break;
                nodes[index] = rightNode;
                if (rightNode != null)
                    rightNode.index = index;
                index = rightIndex;
            }
        }
        nodes[index] = node;
        node.index = index;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BinaryHeap))
            return false;
        BinaryHeap other = (BinaryHeap) obj;
        if (other.size != size)
            return false;
        Node[] nodes1 = this.nodes, nodes2 = other.nodes;
        for (int i = 0, n = size; i < n; i++)
            if (nodes1[i].value != nodes2[i].value)
                return false;
        return true;
    }

    @Override
    public int hashCode() {
        int h = 1;
        Node[] nodes = this.nodes;
        for (int i = 0, n = size; i < n; i++)
            h = h * 31 + Float.floatToIntBits(nodes[i].value);
        return h;
    }

    @Override
    public String toString() {
        if (size == 0)
            return "[]";
        Node[] nodes = this.nodes;
        StrBuilder buffer = new StrBuilder(32);
        buffer.append('[');
        buffer.append(nodes[0].value);
        for (int i = 1; i < size; i++) {
            buffer.append(", ");
            buffer.append(nodes[i].value);
        }
        buffer.append(']');
        return buffer.toString();
    }

    /**
     * A binary heap node.
     *
     * @author Nathan Sweet
     */
    static public class Node {
        float value;
        int index;

        /**
         * @param value The initial value for the node. To change the value, use
         *              {@link BinaryHeap#add(Node, float)} if the node is not in the
         *              heap, or {@link BinaryHeap#setValue(Node, float)} if the node is
         *              in the heap.
         */
        public Node(float value) {
            this.value = value;
        }

        public float getValue() {
            return value;
        }

        @Override
        public String toString() {
            return Float.toString(value);
        }
    }
}
