package com.antoshkaplus.model;

import java.util.*;

/**
 * Created by antoshkaplus on 10/16/14.
 */
public class ElevatorSystem implements  ElevatorController.Listener {
    // here we have three states for the elevator
    // idle => target => serving => idle

    private EnumMap<Direction, List<Integer>> requests;

    private List<ServingElevatorController> servingElevatorControllers = Collections.synchronizedList(new ArrayList<>());
    private List<TargetElevatorController> targetElevatorControllers = Collections.synchronizedList(new ArrayList<>());
    private List<BuildingElevator> idleElevators = Collections.synchronizedList(new ArrayList<>());


    public ElevatorSystem() {
        requests = new EnumMap<Direction, List<Integer>>(Direction.class);
        requests.put(Direction.DOWN, Collections.synchronizedList(new ArrayList<Integer>()));
        requests.put(Direction.UP, Collections.synchronizedList(new ArrayList<Integer>()));
    }

    @Override
    public void onFinish(ElevatorController controller) {
        if (controller instanceof TargetElevatorController) {
            TargetElevatorController tcl = (TargetElevatorController)controller;
            targetElevatorControllers.remove(controller);
            servingElevatorControllers.add(new ServingElevatorController(controller.getElevator(), tcl.getRequest().direction));
        } else if (controller instanceof ServingElevatorController) {
            servingElevatorControllers.remove(controller);
            idleElevators.add(controller.getElevator());
            // try to make busy with request
        }
    }

    public void addElevator(BuildingElevator elevator) {
        idleElevators.add(elevator);
    }

    public void addRequest(ElevatorRequest request) {
        Direction direction = request.direction;
        int floor = request.floor;
        if (requests.get(direction).contains(floor)) {
            return;
        }
        for (ServingElevatorController c : servingElevatorControllers) {
            if (c.getElevator().getControls().getDirection() == request.direction && c.addRequest(floor)) {
                // request added to controller to handle
                return;
            }
        }
        if (idleElevators.isEmpty()) {
            requests.get(direction).add(floor);
            return;
        }
        int i = idleElevators.size() - 1;
        BuildingElevator el = idleElevators.get(i);
        idleElevators.remove(i);
        targetElevatorControllers.add(new TargetElevatorController(el, request));
    }
}