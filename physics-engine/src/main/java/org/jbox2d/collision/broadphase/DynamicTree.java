/*******************************************************************************
 * Copyright (c) 2013, Daniel Murphy
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package org.jbox2d.collision.broadphase;

import org.jbox2d.callbacks.TreeCallback;
import org.jbox2d.callbacks.TreeRayCastCallback;
import org.jbox2d.collision.AABB;
import org.jbox2d.collision.RayCastInput;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Settings;
import org.jbox2d.common.Vector2;

/**
 * A dynamic tree arranges data in a binary tree to accelerate queries such as volume queries and
 * ray casts. Leafs are proxies with an AABB. In the tree we expand the proxy AABB by _fatAABBFactor
 * so that the proxy AABB is bigger than the client object. This allows the client object to move by
 * small amounts without triggering a tree update.
 *
 * @author daniel
 */
public class DynamicTree implements BroadPhaseStrategy {
    private static final int NULL_NODE = -1;

    private DynamicTreeNode m_root;
    private DynamicTreeNode[] m_nodes;
    private int m_nodeCount;
    private int m_nodeCapacity;

    private int m_freeList;

    private DynamicTreeNode[] nodeStack = new DynamicTreeNode[20];
    private int nodeStackIndex;

    public DynamicTree() {
        m_root = null;
        m_nodeCount = 0;
        m_nodeCapacity = 16;
        m_nodes = new DynamicTreeNode[16];

        // Build a linked list for the free list.
        for (int i = m_nodeCapacity - 1; i >= 0; i--) {
            m_nodes[i] = new DynamicTreeNode(i);
            m_nodes[i].parent = (i == m_nodeCapacity - 1) ? null : m_nodes[i + 1];
            m_nodes[i].height = -1;
        }
        m_freeList = 0;
    }

    @Override
    public final int createProxy(AABB aabb, Object userData) {
        assert (aabb.isValid());
        DynamicTreeNode node = allocateNode();
        int proxyId = node.id;
        // Fatten the aabb
        AABB nodeAABB = node.aabb;
        nodeAABB.lowerBound.x = aabb.lowerBound.x - Settings.aabbExtension;
        nodeAABB.lowerBound.y = aabb.lowerBound.y - Settings.aabbExtension;
        nodeAABB.upperBound.x = aabb.upperBound.x + Settings.aabbExtension;
        nodeAABB.upperBound.y = aabb.upperBound.y + Settings.aabbExtension;
        node.userData = userData;

        insertLeaf(proxyId);

        return proxyId;
    }

    @Override
    public final void destroyProxy(int proxyId) {
        assert (0 <= proxyId && proxyId < m_nodeCapacity);
        DynamicTreeNode node = m_nodes[proxyId];
        assert (node.child1 == null);

        removeLeaf(node);
        freeNode(node);
    }

    @Override
    public final boolean moveProxy(int proxyId, AABB aabb, Vector2 displacement) {
        assert (aabb.isValid());
        assert (0 <= proxyId && proxyId < m_nodeCapacity);
        DynamicTreeNode node = m_nodes[proxyId];
        assert (node.child1 == null);

        AABB nodeAABB = node.aabb;
        // if (nodeAABB.contains(aabb)) {
        if (nodeAABB.lowerBound.x <= aabb.lowerBound.x && nodeAABB.lowerBound.y <= aabb.lowerBound.y
                && aabb.upperBound.x <= nodeAABB.upperBound.x && aabb.upperBound.y <= nodeAABB.upperBound.y) {
            return false;
        }

        removeLeaf(node);

        // Extend AABB
        Vector2 lowerBound = nodeAABB.lowerBound;
        Vector2 upperBound = nodeAABB.upperBound;
        lowerBound.x = aabb.lowerBound.x - Settings.aabbExtension;
        lowerBound.y = aabb.lowerBound.y - Settings.aabbExtension;
        upperBound.x = aabb.upperBound.x + Settings.aabbExtension;
        upperBound.y = aabb.upperBound.y + Settings.aabbExtension;

        // Predict AABB displacement.
        float dx = displacement.x * Settings.aabbMultiplier;
        float dy = displacement.y * Settings.aabbMultiplier;
        if (dx < 0.0f) {
            lowerBound.x += dx;
        } else {
            upperBound.x += dx;
        }

        if (dy < 0.0f) {
            lowerBound.y += dy;
        } else {
            upperBound.y += dy;
        }

        insertLeaf(proxyId);
        return true;
    }

    @Override
    public final Object getUserData(int proxyId) {
        assert (0 <= proxyId && proxyId < m_nodeCapacity);
        return m_nodes[proxyId].userData;
    }

    @Override
    public final AABB getFatAABB(int proxyId) {
        assert (0 <= proxyId && proxyId < m_nodeCapacity);
        return m_nodes[proxyId].aabb;
    }

    @Override
    public final void query(TreeCallback callback, AABB aabb) {
        assert (aabb.isValid());
        nodeStackIndex = 0;
        nodeStack[nodeStackIndex++] = m_root;

        while (nodeStackIndex > 0) {
            DynamicTreeNode node = nodeStack[--nodeStackIndex];
            if (node == null) {
                continue;
            }

            if (AABB.testOverlap(node.aabb, aabb)) {
                if (node.child1 == null) {
                    boolean proceed = callback.treeCallback(node.id);
                    if (!proceed) {
                        return;
                    }
                } else {
                    if (nodeStack.length - nodeStackIndex - 2 <= 0) {
                        DynamicTreeNode[] newBuffer = new DynamicTreeNode[(nodeStack.length << 1)];
                        System.arraycopy(nodeStack, 0, newBuffer, 0, nodeStack.length);
                        nodeStack = newBuffer;
                    }
                    nodeStack[nodeStackIndex++] = node.child1;
                    nodeStack[nodeStackIndex++] = node.child2;
                }
            }
        }
    }

    private final Vector2 r = new Vector2();
    private final AABB aabb = new AABB();
    private final RayCastInput subInput = new RayCastInput();

    @Override
    public void raycast(TreeRayCastCallback callback, RayCastInput input) {
        Vector2 p1 = input.p1;
        Vector2 p2 = input.p2;
        float p1x = p1.x, p2x = p2.x, p1y = p1.y, p2y = p2.y;
        float vx, vy;
        float rx, ry;
        float absVx, absVy;
        float cx, cy;
        float hx, hy;
        float tempx, tempy;
        r.x = p2x - p1x;
        r.y = p2y - p1y;
        assert ((r.x * r.x + r.y * r.y) > 0f);
        r.normalize();
        rx = r.x;
        ry = r.y;

        // v is perpendicular to the segment.
        vx = -1f * ry;
        vy = rx;
        absVx = MathUtils.abs(vx);
        absVy = MathUtils.abs(vy);

        // Separating axis for segment (Gino, p80).
        // |dot(v, p1 - c)| > dot(|v|, h)

        float maxFraction = input.maxFraction;

        // Build a bounding box for the segment.
        AABB segAABB = aabb;
        // Vec2 t = p1 + maxFraction * (p2 - p1);
        // before inline
        // temp.set(p2).subLocal(p1).mulLocal(maxFraction).addLocal(p1);
        // Vec2.minToOut(p1, temp, segAABB.lowerBound);
        // Vec2.maxToOut(p1, temp, segAABB.upperBound);
        tempx = (p2x - p1x) * maxFraction + p1x;
        tempy = (p2y - p1y) * maxFraction + p1y;
        segAABB.lowerBound.x = p1x < tempx ? p1x : tempx;
        segAABB.lowerBound.y = p1y < tempy ? p1y : tempy;
        segAABB.upperBound.x = p1x > tempx ? p1x : tempx;
        segAABB.upperBound.y = p1y > tempy ? p1y : tempy;
        // end inline

        nodeStackIndex = 0;
        nodeStack[nodeStackIndex++] = m_root;
        while (nodeStackIndex > 0) {
            DynamicTreeNode node = nodeStack[--nodeStackIndex];
            if (node == null) {
                continue;
            }

            AABB nodeAABB = node.aabb;
            if (!AABB.testOverlap(nodeAABB, segAABB)) {
                continue;
            }

            // Separating axis for segment (Gino, p80).
            // |dot(v, p1 - c)| > dot(|v|, h)
            // node.aabb.getCenterToOut(c);
            // node.aabb.getExtentsToOut(h);
            cx = (nodeAABB.lowerBound.x + nodeAABB.upperBound.x) * .5f;
            cy = (nodeAABB.lowerBound.y + nodeAABB.upperBound.y) * .5f;
            hx = (nodeAABB.upperBound.x - nodeAABB.lowerBound.x) * .5f;
            hy = (nodeAABB.upperBound.y - nodeAABB.lowerBound.y) * .5f;
            tempx = p1x - cx;
            tempy = p1y - cy;
            float separation = MathUtils.abs(vx * tempx + vy * tempy) - (absVx * hx + absVy * hy);
            if (separation > 0.0f) {
                continue;
            }

            if (node.child1 == null) {
                subInput.p1.x = p1x;
                subInput.p1.y = p1y;
                subInput.p2.x = p2x;
                subInput.p2.y = p2y;
                subInput.maxFraction = maxFraction;

                float value = callback.raycastCallback(subInput, node.id);

                if (value == 0.0f) {
                    // The client has terminated the ray cast.
                    return;
                }

                if (value > 0.0f) {
                    // Update segment bounding box.
                    maxFraction = value;
                    // temp.set(p2).subLocal(p1).mulLocal(maxFraction).addLocal(p1);
                    // Vec2.minToOut(p1, temp, segAABB.lowerBound);
                    // Vec2.maxToOut(p1, temp, segAABB.upperBound);
                    tempx = (p2x - p1x) * maxFraction + p1x;
                    tempy = (p2y - p1y) * maxFraction + p1y;
                    segAABB.lowerBound.x = p1x < tempx ? p1x : tempx;
                    segAABB.lowerBound.y = p1y < tempy ? p1y : tempy;
                    segAABB.upperBound.x = p1x > tempx ? p1x : tempx;
                    segAABB.upperBound.y = p1y > tempy ? p1y : tempy;
                }
            } else {
                if (nodeStack.length - nodeStackIndex - 2 <= 0) {
                    DynamicTreeNode[] newBuffer = new DynamicTreeNode[(nodeStack.length << 1)];
                    System.arraycopy(nodeStack, 0, newBuffer, 0, nodeStack.length);
                    nodeStack = newBuffer;
                }
                nodeStack[nodeStackIndex++] = node.child1;
                nodeStack[nodeStackIndex++] = node.child2;
            }
        }
    }

    @Override
    public final int computeHeight() {
        return computeHeight(m_root);
    }

    private int computeHeight(DynamicTreeNode node) {
        assert (0 <= node.id && node.id < m_nodeCapacity);

        if (node.child1 == null) {
            return 0;
        }
        int height1 = computeHeight(node.child1);
        int height2 = computeHeight(node.child2);
        return 1 + MathUtils.max(height1, height2);
    }

    @Override
    public int getHeight() {
        if (m_root == null) {
            return 0;
        }
        return m_root.height;
    }

    @Override
    public int getMaxBalance() {
        int maxBalance = 0;
        for (int i = 0; i < m_nodeCapacity; ++i) {
            DynamicTreeNode node = m_nodes[i];
            if (node.height <= 1) {
                continue;
            }

            assert (node.child1 == null == false);

            DynamicTreeNode child1 = node.child1;
            DynamicTreeNode child2 = node.child2;
            int balance = MathUtils.abs(child2.height - child1.height);
            maxBalance = MathUtils.max(maxBalance, balance);
        }

        return maxBalance;
    }

    @Override
    public float getAreaRatio() {
        if (m_root == null) {
            return 0.0f;
        }

        DynamicTreeNode root = m_root;
        float rootArea = root.aabb.getPerimeter();

        float totalArea = 0.0f;
        for (int i = 0; i < m_nodeCapacity; ++i) {
            DynamicTreeNode node = m_nodes[i];
            if (node.height < 0) {
                // Free node in pool
                continue;
            }

            totalArea += node.aabb.getPerimeter();
        }

        return totalArea / rootArea;
    }

    private DynamicTreeNode allocateNode() {
        if (m_freeList == NULL_NODE) {
            assert (m_nodeCount == m_nodeCapacity);

            DynamicTreeNode[] old = m_nodes;
            m_nodeCapacity <<= 1;
            m_nodes = new DynamicTreeNode[m_nodeCapacity];
            System.arraycopy(old, 0, m_nodes, 0, old.length);

            // Build a linked list for the free list.
            for (int i = m_nodeCapacity - 1; i >= m_nodeCount; i--) {
                m_nodes[i] = new DynamicTreeNode(i);
                m_nodes[i].parent = (i == m_nodeCapacity - 1) ? null : m_nodes[i + 1];
                m_nodes[i].height = -1;
            }
            m_freeList = m_nodeCount;
        }
        int nodeId = m_freeList;
        DynamicTreeNode treeNode = m_nodes[nodeId];
        m_freeList = treeNode.parent != null ? treeNode.parent.id : NULL_NODE;

        treeNode.parent = null;
        treeNode.child1 = null;
        treeNode.child2 = null;
        treeNode.height = 0;
        treeNode.userData = null;
        ++m_nodeCount;
        return treeNode;
    }

    /**
     * returns a node to the pool
     */
    private void freeNode(DynamicTreeNode node) {
        assert (node != null);
        assert (0 < m_nodeCount);
        node.parent = m_freeList == NULL_NODE ? null : m_nodes[m_freeList];
        node.height = -1;
        m_freeList = node.id;
        m_nodeCount--;
    }

    private final AABB combinedAABB = new AABB();

    private void insertLeaf(int leaf_index) {
        DynamicTreeNode leaf = m_nodes[leaf_index];
        if (m_root == null) {
            m_root = leaf;
            m_root.parent = null;
            return;
        }

        // find the best sibling
        AABB leafAABB = leaf.aabb;
        DynamicTreeNode index = m_root;
        while (index.child1 != null) {
            DynamicTreeNode node = index;
            DynamicTreeNode child1 = node.child1;
            DynamicTreeNode child2 = node.child2;

            float area = node.aabb.getPerimeter();

            combinedAABB.combine(node.aabb, leafAABB);
            float combinedArea = combinedAABB.getPerimeter();

            // Cost of creating a new parent for this node and the new leaf
            float cost = 2.0f * combinedArea;

            // Minimum cost of pushing the leaf further down the tree
            float inheritanceCost = 2.0f * (combinedArea - area);

            // Cost of descending into child1
            float cost1;
            if (child1.child1 == null) {
                combinedAABB.combine(leafAABB, child1.aabb);
                cost1 = combinedAABB.getPerimeter() + inheritanceCost;
            } else {
                combinedAABB.combine(leafAABB, child1.aabb);
                float oldArea = child1.aabb.getPerimeter();
                float newArea = combinedAABB.getPerimeter();
                cost1 = (newArea - oldArea) + inheritanceCost;
            }

            // Cost of descending into child2
            float cost2;
            if (child2.child1 == null) {
                combinedAABB.combine(leafAABB, child2.aabb);
                cost2 = combinedAABB.getPerimeter() + inheritanceCost;
            } else {
                combinedAABB.combine(leafAABB, child2.aabb);
                float oldArea = child2.aabb.getPerimeter();
                float newArea = combinedAABB.getPerimeter();
                cost2 = newArea - oldArea + inheritanceCost;
            }

            // Descend according to the minimum cost.
            if (cost < cost1 && cost < cost2) {
                break;
            }

            // Descend
            if (cost1 < cost2) {
                index = child1;
            } else {
                index = child2;
            }
        }

        DynamicTreeNode sibling = index;
        DynamicTreeNode oldParent = m_nodes[sibling.id].parent;
        DynamicTreeNode newParent = allocateNode();
        newParent.parent = oldParent;
        newParent.userData = null;
        newParent.aabb.combine(leafAABB, sibling.aabb);
        newParent.height = sibling.height + 1;

        if (oldParent != null) {
            // The sibling was not the root.
            if (oldParent.child1 == sibling) {
                oldParent.child1 = newParent;
            } else {
                oldParent.child2 = newParent;
            }

            newParent.child1 = sibling;
            newParent.child2 = leaf;
            sibling.parent = newParent;
            leaf.parent = newParent;
        } else {
            // The sibling was the root.
            newParent.child1 = sibling;
            newParent.child2 = leaf;
            sibling.parent = newParent;
            leaf.parent = newParent;
            m_root = newParent;
        }

        // Walk back up the tree fixing heights and AABBs
        index = leaf.parent;
        while (index != null) {
            index = balance(index);

            DynamicTreeNode child1 = index.child1;
            DynamicTreeNode child2 = index.child2;

            assert (child1 != null);
            assert (child2 != null);

            index.height = 1 + MathUtils.max(child1.height, child2.height);
            index.aabb.combine(child1.aabb, child2.aabb);

            index = index.parent;
        }
        // validate();
    }

    private void removeLeaf(DynamicTreeNode leaf) {
        if (leaf == m_root) {
            m_root = null;
            return;
        }

        DynamicTreeNode parent = leaf.parent;
        DynamicTreeNode grandParent = parent.parent;
        DynamicTreeNode sibling;
        if (parent.child1 == leaf) {
            sibling = parent.child2;
        } else {
            sibling = parent.child1;
        }

        if (grandParent != null) {
            // Destroy parent and connect sibling to grandParent.
            if (grandParent.child1 == parent) {
                grandParent.child1 = sibling;
            } else {
                grandParent.child2 = sibling;
            }
            sibling.parent = grandParent;
            freeNode(parent);

            // Adjust ancestor bounds.
            DynamicTreeNode index = grandParent;
            while (index != null) {
                index = balance(index);

                DynamicTreeNode child1 = index.child1;
                DynamicTreeNode child2 = index.child2;

                index.aabb.combine(child1.aabb, child2.aabb);
                index.height = 1 + MathUtils.max(child1.height, child2.height);

                index = index.parent;
            }
        } else {
            m_root = sibling;
            sibling.parent = null;
            freeNode(parent);
        }

        // validate();
    }

    // Perform a left or right rotation if node A is imbalanced.
    // Returns the new root index.
    private DynamicTreeNode balance(DynamicTreeNode iA) {
        assert (iA != null);

        if (iA.child1 == null || iA.height < 2) {
            return iA;
        }

        DynamicTreeNode iB = iA.child1;
        DynamicTreeNode iC = iA.child2;
        assert (0 <= iB.id && iB.id < m_nodeCapacity);
        assert (0 <= iC.id && iC.id < m_nodeCapacity);

        int balance = iC.height - iB.height;

        // Rotate C up
        if (balance > 1) {
            DynamicTreeNode iF = iC.child1;
            DynamicTreeNode iG = iC.child2;
            assert (iF != null);
            assert (iG != null);
            assert (0 <= iF.id && iF.id < m_nodeCapacity);
            assert (0 <= iG.id && iG.id < m_nodeCapacity);

            // Swap A and C
            iC.child1 = iA;
            iC.parent = iA.parent;
            iA.parent = iC;

            // A's old parent should point to C
            if (iC.parent != null) {
                if (iC.parent.child1 == iA) {
                    iC.parent.child1 = iC;
                } else {
                    assert (iC.parent.child2 == iA);
                    iC.parent.child2 = iC;
                }
            } else {
                m_root = iC;
            }

            // Rotate
            if (iF.height > iG.height) {
                iC.child2 = iF;
                iA.child2 = iG;
                iG.parent = iA;
                iA.aabb.combine(iB.aabb, iG.aabb);
                iC.aabb.combine(iA.aabb, iF.aabb);

                iA.height = 1 + MathUtils.max(iB.height, iG.height);
                iC.height = 1 + MathUtils.max(iA.height, iF.height);
            } else {
                iC.child2 = iG;
                iA.child2 = iF;
                iF.parent = iA;
                iA.aabb.combine(iB.aabb, iF.aabb);
                iC.aabb.combine(iA.aabb, iG.aabb);

                iA.height = 1 + MathUtils.max(iB.height, iF.height);
                iC.height = 1 + MathUtils.max(iA.height, iG.height);
            }

            return iC;
        }

        // Rotate B up
        if (balance < -1) {
            DynamicTreeNode iD = iB.child1;
            DynamicTreeNode iE = iB.child2;
            assert (0 <= iD.id && iD.id < m_nodeCapacity);
            assert (0 <= iE.id && iE.id < m_nodeCapacity);

            // Swap A and B
            iB.child1 = iA;
            iB.parent = iA.parent;
            iA.parent = iB;

            // A's old parent should point to B
            if (iB.parent != null) {
                if (iB.parent.child1 == iA) {
                    iB.parent.child1 = iB;
                } else {
                    assert (iB.parent.child2 == iA);
                    iB.parent.child2 = iB;
                }
            } else {
                m_root = iB;
            }

            // Rotate
            if (iD.height > iE.height) {
                iB.child2 = iD;
                iA.child1 = iE;
                iE.parent = iA;
                iA.aabb.combine(iC.aabb, iE.aabb);
                iB.aabb.combine(iA.aabb, iD.aabb);

                iA.height = 1 + MathUtils.max(iC.height, iE.height);
                iB.height = 1 + MathUtils.max(iA.height, iD.height);
            } else {
                iB.child2 = iE;
                iA.child1 = iD;
                iD.parent = iA;
                iA.aabb.combine(iC.aabb, iD.aabb);
                iB.aabb.combine(iA.aabb, iE.aabb);

                iA.height = 1 + MathUtils.max(iC.height, iD.height);
                iB.height = 1 + MathUtils.max(iA.height, iE.height);
            }

            return iB;
        }

        return iA;
    }
}
