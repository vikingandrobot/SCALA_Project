# Development

The development folder for the project (source code, etc.)

## Add a new template (Warning)
If you add a new template and use the main template as a base, you need to add the following code to your template :

```
(implicit request: play.api.mvc.RequestHeader)
```

And to the controller that calls your template:

```
def action = Action.async { implicit request =>
    ...
}
```

## Connected user session
A session is created when a user is logged. The session's name is `connected` and it provides the username of the connected user.
