# ModLauncher
Launches mods the easy way

## To Do:
- [ ] Region CSS sizes (fxml)
- [ ] Join server `MeldInfoPanel`
  - [ ] Offline server info viewer / instance
  - [ ] Update warnings
    - [x] New Mods 
    - [x] Unacknowledged warnings
    - [ ] Delete warnings
  - [ ] Version changing
  - [ ] Downloading mods
    - [ ] Proper handling of IOException
    - [ ] See TODOs in `WebDownloader` and `GameInstance`.
  - [ ] akdfjsdkfjhweiudjsakdjcskdjfhkjdsjvkdf
  - [x] Starting the game
    - [x] FIX: Wrong assets used.
- [ ] Fix incorrect placement and styling of errors `ButtonPanel`
- [ ] Hardcoded value in `CentrePanel`
- [ ] Implement name length limit. Description length limit. Modern MC Component text formatting `ServerEntry`
- [ ] Favicon from server ping (wtf does this mean?? I forgot) `ServerEntry`
- [ ] Pinging when re-showing the Selection panel `MainWindowController`
- [ ] Explore default minecraft behaviour with invalid formatting codes + Fix some formatting combinations not working `FormattedTextParser`
- [x] NullPointerException when no MeldData `InstanceManager`/`GameInstance`
  - [x] Buttons not being enabled when server is pinging.
- [ ] Deleting Instance With callback
  - [ ] Backup progress indicator
- [ ] Hash verifying (done?????) `MeldData`
- [ ] Remove Debug + Do something about the <0.5 ping time display bug assuming timeout even when there is ping `Pinger`
- [ ] Resource + Shader packs
- [ ] Modrinth custom `User-Agent` header.
- [ ] Translations
- [ ] Extra client mods that aren't specified by the server.
- [ ] Everything else that I haven't though of.
- [ ] Bugs Bugs Bugs!!!
