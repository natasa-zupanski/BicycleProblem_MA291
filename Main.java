
class Main {

    protected static class Constants {
        final double gravity = 9.81;
        final double kinematic_velocity_of_air = 0;
        final double cross_section_spoked_wheel = 0;
        final double cross_section_disc_wheel = 0;
        final double radius_spoked_wheel = 0;
        final double radius_disc_wheel = 0;
    }

    protected class Constant_Per_Course {
        double wind_speed;
        double wind_direction;
        double air_direction;
        double mass_bike_and_rider;
        double cross_section_rider_and_bike;
        int power;
    }

    protected class Var_Per_Instance {
        double grade;
        double bike_direction;
    }

    protected class Inputs {
        double step_length;
        double number_steps;
        Var_Per_Instance[] situations;
        Constant_Per_Course course_specs;
    }

    public static void main(String[] args) {

    }

    private void dragOnBikeExceptWheels() {

    }

    private void dragOnFrontWheel() {

    }

    private void dragOnBackWheel() {

    }

    private void gravitationalResistance() {

    }

    private void rollingResistance() {

    }

    private void frictionInAirOnWheels() {

    }

}
