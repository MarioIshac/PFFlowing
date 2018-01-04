package me.theeninja.pfflowing;

public interface StringSerializable<T> {
    String serialize();
    T deserialize(String string);
}
