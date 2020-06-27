# Todo List

This todo list application for Android is written in Java and with a modern MVVM architecture using the repository pattern and with several of the Android architecture components:

- Navigation component
- ViewModel
- Room
- LiveData
- Data Binding
- View Binding

#### The app has the following features:

- Add and edit tasks
- Search for tasks
- Add time based notifications to tasks 
- Add location based notifications to tasks through geofencing
- Update and remove notifications, with undo
- Set a priority level for each task (low, medium, high)
- Set tasks to completed, and back to uncompleted
- Delete individual, all completed, or all tasks
- Undo task deletion
- UI for both dark mode and light mode, which can be toggled from within the app

In order to use geofencing you'll need to create your own Google Maps API key.

#### Potential improvements:

- Recurring notifications, for example by scheduling a timed based notification to happen once a day
- (Adaptive) launcher icons

I have also made an iOS version: [iOS Todo List](https://github.com/fredrik9000/TodoList_iOS)

## Screenshots

![task_list_dark](https://user-images.githubusercontent.com/13121494/85931780-2101f300-b8c7-11ea-9076-970c98debe60.png)

![edit_task_dark](https://user-images.githubusercontent.com/13121494/85931774-19424e80-b8c7-11ea-8c01-f7e027d70412.png)

![geofence_dark](https://user-images.githubusercontent.com/13121494/85931778-20695c80-b8c7-11ea-83ba-9a75cc2c7e27.png)

![task_list_light](https://user-images.githubusercontent.com/13121494/85931781-2101f300-b8c7-11ea-9dca-8334d93213fe.png)

![edit_task_light](https://user-images.githubusercontent.com/13121494/85931777-1fd0c600-b8c7-11ea-911d-0717045bff56.png)

![geofence_light](https://user-images.githubusercontent.com/13121494/85931779-2101f300-b8c7-11ea-8708-35901de0bc6c.png)