# TODO: Fix Compilation Errors

## Task: Resolve Spring Boot compilation errors

## Steps to Complete:

### 1. Add `duration` field to Module entity
- [ ] Add `duration` field (Integer) to Module.java entity

### 2. Add missing methods to SlotRepository
- [ ] Add `existsByWeekIdAndTrainerIdAndDayOfWeekAndSlotNumber()` method
- [ ] Add `findByTrainerId()` method

### 3. Fix TimetableService.java
- [ ] Replace `slot.getCourse()` with `slot.getModule().getCourse()`
- [ ] Convert DayOfWeek enum to String when building response

### 4. Fix SchedulingService.java
- [ ] Use existing repository method `findByTrainerAndDayAndSlot` instead of missing `existsByWeekIdAndTrainerIdAndDayOfWeekAndSlotNumber`
- [ ] Convert String dayOfWeek to DayOfWeek enum for repository queries
- [ ] Replace `slot.getCourse()` with `slot.getModule().getCourse()`
- [ ] Convert DayOfWeek enum to String when building response

## Notes:
- Module entity needs `duration` field to match ModuleRequest and ModuleResponse DTOs
- Slot entity has `module` field, not `course` field - need to navigate through module to get course
- SlotRequest/SlotResponse/TimetableResponse use String for dayOfWeek, but Slot entity uses DayOfWeek enum
- Need to convert between String and DayOfWeek enum in services
