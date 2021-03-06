/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.ide.common.res2;

import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.resources.ResourceType;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.android.SdkConstants.ATTR_NAME;
import static com.android.SdkConstants.TAG_DECLARE_STYLEABLE;
import static com.android.ide.common.res2.DataFile.FileType;
import static com.android.ide.common.res2.ResourceFile.ATTR_QUALIFIER;

/**
 * Implementation of {@link DataMerger} for {@link ResourceSet}, {@link ResourceItem}, and
 * {@link ResourceFile}.
 */
public class ResourceMerger extends DataMerger<ResourceItem, ResourceFile, ResourceSet> {

    /**
     * Override of the normal ResourceItem to handle merged item cases.
     * This is mostly to deal with items that do not have a matching source file.
     * This override the method returning the qualifier or the source type, to directly
     * return a value instead of relying on a source file (since merged items don't have any).
     */
    private static class MergedResourceItem extends ResourceItem {

        @NonNull
        private final String mQualifiers;

        /**
         * Constructs the object with a name, type and optional value.
         *
         * Note that the object is not fully usable as-is. It must be added to a ResourceFile first.
         *
         * @param name  the name of the resource
         * @param type  the type of the resource
         * @param qualifiers the qualifiers of the resource
         * @param value an optional Node that represents the resource value.
         */
        public MergedResourceItem(
                @NonNull String name,
                @NonNull ResourceType type,
                @NonNull String qualifiers,
                @Nullable Node value) {
            super(name, type, value);
            mQualifiers = qualifiers;
        }

        @NonNull
        @Override
        public String getQualifiers() {
            return mQualifiers;
        }

        @Override
        @NonNull
        public FileType getSourceType() {
            return FileType.MULTI;
        }
    }

    /**
     * Map of items that are purely results of merges (ie item that made up of several
     * original items). The first map key is the associated qualifier for the items,
     * the second map key is the item name.
     */
    protected final Map<String, Map<String, ResourceItem>> mMergedItems = Maps.newHashMap();


    @Override
    protected ResourceSet createFromXml(Node node) {
        ResourceSet set = new ResourceSet("");
        return (ResourceSet) set.createFromXml(node);
    }

    @Override
    protected boolean requiresMerge(@NonNull String dataItemKey) {
        return dataItemKey.startsWith("declare-styleable/");
    }

    @Override
    protected void mergeItems(
            @NonNull String dataItemKey,
            @NonNull List<ResourceItem> items,
            @NonNull MergeConsumer<ResourceItem> consumer) throws MergingException {
        boolean touched = false; // touched becomes true if one is touched.
        boolean removed = true; // removed stays true if all items are removed.
        for (ResourceItem item : items) {
            touched |= item.isTouched();
            removed &= item.isRemoved();
        }

        // get the name of the item (the key is the full key not just the same).
        ResourceItem sourceItem = items.get(0);
        String itemName = sourceItem.getName();
        String qualifier = sourceItem.getQualifiers();
        // get the matching mergedItem
        ResourceItem previouslyWrittenItem = getMergedItem(qualifier, itemName);

        try {
            if (touched || (previouslyWrittenItem == null && !removed)) {
                DocumentBuilder builder = mFactory.newDocumentBuilder();
                Document document = builder.newDocument();

                Node declareStyleableNode = document.createElementNS(null, TAG_DECLARE_STYLEABLE);

                Attr nameAttr = document.createAttribute(ATTR_NAME);
                nameAttr.setValue(itemName);
                declareStyleableNode.getAttributes().setNamedItem(nameAttr);

                // loop through all the items and gather a unique list of nodes.
                // because we start with the lower priority items, this means that attr with
                // format inside declare-styleable will be processed first, and added first
                // while the redundant attr (with no format) will be ignored.
                Set<String> attrs = Sets.newHashSet();

                for (ResourceItem item : items) {
                    if (!item.isRemoved()) {
                        Node oldDeclareStyleable = item.getValue();
                        if (oldDeclareStyleable != null) {
                            NodeList children = oldDeclareStyleable.getChildNodes();
                            for (int i = 0; i < children.getLength(); i++) {
                                Node attrNode = children.item(i);
                                if (attrNode.getNodeType() != Node.ELEMENT_NODE) {
                                    continue;
                                }

                                if (SdkConstants.TAG_EAT_COMMENT.equals(attrNode.getLocalName())) {
                                    continue;
                                }

                                // get the name
                                NamedNodeMap attributes = attrNode.getAttributes();
                                nameAttr = (Attr) attributes.getNamedItemNS(null, ATTR_NAME);
                                if (nameAttr == null) {
                                    continue;
                                }

                                String name = nameAttr.getNodeValue();
                                if (attrs.contains(name)) {
                                    continue;
                                }

                                // duplicate the node.
                                attrs.add(name);
                                Node newAttrNode = NodeUtils.duplicateNode(document, attrNode);
                                declareStyleableNode.appendChild(newAttrNode);
                            }
                        }
                    }
                }

                // always write it for now.
                MergedResourceItem newItem = new MergedResourceItem(
                        itemName,
                        sourceItem.getType(),
                        qualifier,
                        declareStyleableNode);

                // check whether the result of the merge is new or touched compared
                // to the previous state.
                //noinspection ConstantConditions
                if (previouslyWrittenItem == null ||
                        !NodeUtils.compareElementNode(newItem.getValue(), previouslyWrittenItem.getValue(), false)) {
                    newItem.setTouched();
                }

                // then always add it both to the list of merged items in the merge
                // and to the consumer.
                addMergedItem(qualifier, newItem);
                consumer.addItem(newItem);

            } else if (previouslyWrittenItem != null) {
                // since we are keeping the previous merge item, no need
                // to add it internally, just send it to the consumer.
                if (removed) {
                    consumer.removeItem(previouslyWrittenItem, null);
                } else {
                    // don't need to compute but we need to write the item anyway since
                    // the item might be written due to the values file requiring (re)writing due
                    // to another res change
                    consumer.addItem(previouslyWrittenItem);
                }
            }
        } catch (ParserConfigurationException e) {
            throw new MergingException(e);
        }
    }

    @Nullable
    private ResourceItem getMergedItem(@NonNull String qualifiers, @NonNull String name) {
        Map<String, ResourceItem> map = mMergedItems.get(qualifiers);
        if (map != null) {
            return map.get(name);
        }

        return null;
    }

    @Override
    protected void loadMergedItems(@NonNull Node mergedItemsNode) {
        // loop on the qualifiers.
        NodeList configurationList = mergedItemsNode.getChildNodes();

        for (int j = 0, n2 = configurationList.getLength(); j < n2; j++) {
            Node configuration = configurationList.item(j);

            if (configuration.getNodeType() != Node.ELEMENT_NODE ||
                    !NODE_CONFIGURATION.equals(configuration.getLocalName())) {
                continue;
            }

            // get the qualifier value.
            Attr qualifierAttr = (Attr) configuration.getAttributes().getNamedItem(
                    ATTR_QUALIFIER);
            if (qualifierAttr == null) {
                continue;
            }

            String qualifier = qualifierAttr.getValue();

            // get the resource items
            NodeList itemList = configuration.getChildNodes();

            for (int k = 0, n3 = itemList.getLength(); k < n3; k++) {
                Node itemNode = itemList.item(k);

                if (itemNode.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                ResourceItem item = getMergedResourceItem(itemNode, qualifier);
                if (item != null) {
                    addMergedItem(qualifier, item);
                }
            }
        }
    }

    @Override
    protected void writeMergedItems(Document document, Node rootNode) {
        Node mergedItemsNode = document.createElement(NODE_MERGED_ITEMS);
        rootNode.appendChild(mergedItemsNode);

        for (String qualifier : mMergedItems.keySet()) {
            Map<String, ResourceItem> itemMap = mMergedItems.get(qualifier);

            Node qualifierNode = document.createElement(NODE_CONFIGURATION);
            NodeUtils.addAttribute(document, qualifierNode, null, ATTR_QUALIFIER,
                    qualifier);

            mergedItemsNode.appendChild(qualifierNode);

            for (ResourceItem item : itemMap.values()) {
                Node adoptedNode = item.getAdoptedNode(document);
                if (adoptedNode != null) {
                    qualifierNode.appendChild(adoptedNode);
                }
            }
        }
    }

    private void addMergedItem(@NonNull String qualifier, @NonNull ResourceItem item) {
        Map<String, ResourceItem> map = mMergedItems.get(qualifier);
        if (map == null) {
            map = Maps.newHashMap();
            mMergedItems.put(qualifier, map);
        }

        map.put(item.getName(), item);
    }

    /**
     * Returns a new ResourceItem object for a given node.
     * @param node the node representing the resource.
     * @return a ResourceItem object or null.
     */
    static MergedResourceItem getMergedResourceItem(@NonNull Node node, @NonNull String qualifiers) {
        ResourceType type = ValueResourceParser2.getType(node, null);
        String name = ValueResourceParser2.getName(node);

        if (name != null && type != null) {
            return new MergedResourceItem(name, type, qualifiers, node);
        }

        return null;
    }
}
