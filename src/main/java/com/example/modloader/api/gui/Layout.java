package com.example.modloader.api.gui;

import java.util.Map;

public interface Layout {
    Map<Integer, Component> arrangeComponents(Map<Component, Object> components);
}