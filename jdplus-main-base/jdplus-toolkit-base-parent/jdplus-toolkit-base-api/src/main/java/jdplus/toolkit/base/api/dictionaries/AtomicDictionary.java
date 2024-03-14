/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.toolkit.base.api.dictionaries;

import nbbrd.design.LombokWorkaround;

import java.util.List;
import java.util.stream.Stream;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.Builder
public class AtomicDictionary implements Dictionary {

    @lombok.Value
    @lombok.Builder
    public static class Item implements Dictionary.Entry {
        
        public static final String EMPTY="", TODO="TODO";

        String name;
        String description;
        String detail;
        Class outputClass;
        
        EntryType type;

        @LombokWorkaround
        public static Item.Builder builder() {
            return new Builder().description(TODO).detail(EMPTY).type(EntryType.Normal);
        }
    }

    public String name;

    @lombok.Getter(lombok.AccessLevel.PRIVATE)
    @lombok.Singular("item")
    List<Item> items;

    @Override
    public Stream<? extends Entry> entries() {
        return items.stream();
    }
}
