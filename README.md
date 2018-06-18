# Scala Project

_Authors: Sathiya Kirushnapillai, Mathieu Monteverde & Michela Zucca_

The GitHub repository for the Scala Project 2018.

## Report and documentation

The source latex files for the documentation and report can be found in the [doc](./doc) folder.

## Source code

The source code (Scala Play project) can be found in the [dev](./dev) folder.

## Deployment

### Database inside Docker

To deploy the application please travel to the [dev/docker](./dev/docker) folder and run:

```
docker-compose up --build
```

This will run a MySQL container with sample data for the application and a PHPMyAdmin container if you want to inspect the database.

The containers will be accessible at:

- MySQL: http://localhost:3306
- PHPMyAdmin: http://localhost:6060 (user and password: root/root)

Please refer to the corresponding Dockerfiles for more information.

### Application

To run the SCALA Play application, please open it in IntellJ and run. You may need to update the configuration to communicate with the database if you changed the installation or decide to use another database infrastructure than the Docker installation.
