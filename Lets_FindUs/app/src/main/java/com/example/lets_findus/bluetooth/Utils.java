package com.example.lets_findus.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.example.lets_findus.bluetooth.GattAttributes.CHARACTERISTIC_ECHO_STRING;
import static com.example.lets_findus.bluetooth.GattAttributes.CHARACTERISTIC_TIME_STRING;
import static com.example.lets_findus.bluetooth.GattAttributes.CLIENT_CONFIGURATION_DESCRIPTOR_SHORT_ID;
import static com.example.lets_findus.bluetooth.GattAttributes.SERVICE_STRING;


public class Utils {

    //TODO: probabilmente inutile, rimuovere
    public static String byteArrayInHexFormat(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{ ");
        for (int i = 0; i < byteArray.length; i++) {
            if (i > 0) {
                stringBuilder.append(", ");
            }
            String hexString = byteToHex(byteArray[i]);
            stringBuilder.append(hexString);
        }
        stringBuilder.append(" }");

        return stringBuilder.toString();
    }

    public static byte[] bytesFromString(String string) {
        return string.getBytes(StandardCharsets.UTF_8);
    }

    //TODO: probabilmente inutile, rimuovere
    private static String byteToHex(byte b) {
        char char1 = Character.forDigit((b & 0xF0) >> 4, 16);
        char char2 = Character.forDigit((b & 0x0F), 16);

        return String.format("0x%1$s%2$s", char1, char2);
    }

    public static Bitmap byteToImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    public static String byteToString(byte[] data) {
        return new String(data, StandardCharsets.UTF_8);
    }

    private static boolean characteristicMatches(BluetoothGattCharacteristic characteristic, String uuidString) {
        if (characteristic == null) {
            return false;
        }
        UUID uuid = characteristic.getUuid();
        return uuidMatches(uuid.toString(), uuidString);
    }

    private static BluetoothGattCharacteristic findCharacteristic(BluetoothGatt bluetoothGatt, String uuidString) {
        List<BluetoothGattService> serviceList = bluetoothGatt.getServices();
        BluetoothGattService service = findService(serviceList);
        if (service == null) {
            return null;
        }

        List<BluetoothGattCharacteristic> characteristicList = service.getCharacteristics();
        for (BluetoothGattCharacteristic characteristic : characteristicList) {
            if (characteristicMatches(characteristic, uuidString)) {
                return characteristic;
            }
        }

        return null;
    }

    public static List<BluetoothGattCharacteristic> findCharacteristics(BluetoothGatt bluetoothGatt) {
        List<BluetoothGattCharacteristic> matchingCharacteristics = new ArrayList<>();

        List<BluetoothGattService> serviceList = bluetoothGatt.getServices();
        BluetoothGattService service = Utils.findService(serviceList);
        if (service == null) {
            return matchingCharacteristics;
        }

        List<BluetoothGattCharacteristic> characteristicList = service.getCharacteristics();
        for (BluetoothGattCharacteristic characteristic : characteristicList) {
            if (isMatchingCharacteristic(characteristic)) {
                matchingCharacteristics.add(characteristic);
            }
        }

        return matchingCharacteristics;
    }

    public static BluetoothGattDescriptor findClientConfigurationDescriptor(List<BluetoothGattDescriptor> descriptorList) {
        for(BluetoothGattDescriptor descriptor : descriptorList) {
            if (isClientConfigurationDescriptor(descriptor)) {
                return descriptor;
            }
        }

        return null;
    }

    public static BluetoothGattCharacteristic findEchoCharacteristic(BluetoothGatt bluetoothGatt) {
        return findCharacteristic(bluetoothGatt, CHARACTERISTIC_ECHO_STRING);
    }

    private static BluetoothGattService findService(List<BluetoothGattService> serviceList) {
        for (BluetoothGattService service : serviceList) {
            String serviceIdString = service.getUuid().toString();
            if (matchesServiceUuidString(serviceIdString)) {
                return service;
            }
        }
        return null;
    }

    public static byte[] imageToByte(String path) {
        Bitmap bm = BitmapFactory.decodeFile(path);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100 , baos);
        return baos.toByteArray();
    }

    public static byte[] intToByte(int data) {
        return ByteBuffer.allocate(4).putInt(data).array();
    }

    private static boolean isClientConfigurationDescriptor(BluetoothGattDescriptor descriptor) {
        if (descriptor == null) {
            return false;
        }
        UUID uuid = descriptor.getUuid();
        String uuidSubstring = uuid.toString().substring(4, 8);
        return uuidMatches(uuidSubstring, CLIENT_CONFIGURATION_DESCRIPTOR_SHORT_ID);
    }

    public static boolean isEchoCharacteristic(BluetoothGattCharacteristic characteristic) {
        return characteristicMatches(characteristic, CHARACTERISTIC_ECHO_STRING);
    }

    private static boolean isMatchingCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (characteristic == null) {
            return false;
        }
        UUID uuid = characteristic.getUuid();
        return matchesCharacteristicUuidString(uuid.toString());
    }

    public static boolean isTimeCharacteristic(BluetoothGattCharacteristic characteristic) {
        return characteristicMatches(characteristic, CHARACTERISTIC_TIME_STRING);
    }

    private static boolean matchesCharacteristicUuidString(String characteristicIdString) {
        return uuidMatches(characteristicIdString, CHARACTERISTIC_ECHO_STRING, CHARACTERISTIC_TIME_STRING);
    }

    private static boolean matchesServiceUuidString(String serviceIdString) {
        return uuidMatches(serviceIdString, SERVICE_STRING);
    }

    public static boolean requiresConfirmation(BluetoothGattCharacteristic characteristic) {
        return (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) == BluetoothGattCharacteristic.PROPERTY_INDICATE;
    }

    public static boolean requiresResponse(BluetoothGattCharacteristic characteristic) {
        return (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE;
    }

    public static String stringFromBytes(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static boolean uuidMatches(String uuidString, String... matches) {
        for (String match : matches) {
            if (uuidString.equalsIgnoreCase(match)) {
                return true;
            }
        }
        return false;
    }
}