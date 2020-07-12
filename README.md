# Todo List

This todo list application for Android is written in Kotlin and with a modern MVVM architecture. The app uses the repository pattern and several Android architecture components and Jetpack libraries, such as:

- Navigation component
- ViewModel
- Room
- LiveData
- Data Binding
- View Binding
- ConstraintLayout

#### The app has the following features:

- Add and edit tasks
- Search for tasks
- Add time based notifications to tasks 
- Add location based notifications to tasks by setting geofences
- Update and remove notifications, with undo
- Voice input option for entering the tasks title and description
- Set a priority level for each task (low, medium, high)
- Set tasks to completed, and back to uncompleted
- Delete individual, all completed, or all tasks
- Undo task deletion
- UI for both dark mode and light mode, which can be toggled from within the app

In order to use geofencing you'll need to create your own Google Maps API key.

#### Potential improvements:

- Recurring notifications, for example by scheduling a time based notification to happen once a day
- (Adaptive) launcher icons

I have also made an iOS version: [iOS Todo List](https://github.com/fredrik9000/TodoList_iOS)

## Screenshots

![task_list_dark](https://user-images.githubusercontent.com/13121494/87245519-4144bc80-c446-11ea-8baf-cd86aef4edb1.png)

![edit_task_dark](https://user-images.githubusercontent.com/13121494/87245515-3f7af900-c446-11ea-9345-4334f62188e7.png)

![geofence_dark](https://user-images.githubusercontent.com/13121494/87245517-40138f80-c446-11ea-8aac-c1eab2cce7c7.png)

![task_list_light](https://user-images.githubusercontent.com/13121494/87245520-4144bc80-c446-11ea-8f9f-304048f0447c.png)

![edit_task_light](https://user-images.githubusercontent.com/13121494/87245516-40138f80-c446-11ea-954b-9984fa5e7cde.png)

![geofence_light](https://user-images.githubusercontent.com/13121494/87245518-40ac2600-c446-11ea-842e-641ff2c2ed47.png)
