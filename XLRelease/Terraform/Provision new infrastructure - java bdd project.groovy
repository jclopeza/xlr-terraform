// Exported from:        http://kubuntu:5516/#/templates/Folderf09d5151d0184571b0bae064d0bc0219-Release90240ec85f164451a0cca0d7e62d4795/releasefile
// XL Release version:   8.6.3
// Date created:         Fri Aug 09 15:27:08 CEST 2019

xlr {
  template('Provision new infrastructure - java bdd project') {
    folder('Terraform')
    variables {
      listBoxVariable('aws_region') {
        required false
        showOnReleaseStart false
        label 'AWS Region'
        description 'Región de AWS en la que se crearán los recursos y la infraestructura'
        possibleValues 'us-east-1', 'us-east-2', 'us-west-1', 'us-west-2', 'eu-west-1', 'eu-west-2', 'eu-west-3'
        value 'us-east-1'
      }
      listBoxVariable('environment') {
        required false
        showOnReleaseStart false
        label 'Entorno'
        description 'Entorno objeto de la petición de la infraestructura'
        possibleValues 'dev', 'pre', 'pro'
        value 'dev'
      }
      listBoxVariable('instance_type') {
        required false
        showOnReleaseStart false
        label 'Tipo de instancia EC2'
        description 'Tipo de las instancias EC2 que se crearán. Para entornos productivos se recomiendan instancias de mayor capacidad.'
        possibleValues 't2.nano', 't2.micro', 't2.small', 't2.medium', 't2.large', 't2.xlarge', 't2.2xlarge'
        value 't2.micro'
      }
      stringVariable('project_name') {
        required false
        showOnReleaseStart false
        label 'Nombre del proyecto'
        description 'Nombre del proyecto a crear. Debe existir una aplicación del mismo nombre en XL Deploy bajo Applications/Infrastructures'
      }
      stringVariable('public_key_path') {
        required false
        showOnReleaseStart false
        label 'Clave pública'
        description 'Clave pública que se instalará en los servidores remotos para acceder a ellos. Debe estar accesible desde la máquina que aplique las templates de Terraform.'
        value '/home/jcla/.ssh/id_rsa.pub'
      }
      stringVariable('private_key_path') {
        required false
        showOnReleaseStart false
        label 'Clave privada'
        description 'Clave privada para acceder a las instancias EC2. Debe estar accesible desde la máquina de XL Deploy.'
        value '/home/jcla/.ssh/id_rsa'
      }
      listVariable('versions_infrastructure_availables') {
        required false
        showOnReleaseStart false
        label 'Versiones de infraestructura disponibles'
        description 'Versiones de infraestructura disponibles'
      }
      listBoxVariable('version_infrastructure_selected') {
        required false
        showOnReleaseStart false
        label 'Versión de la infraestructura seleccionada'
        possibleValues variable('versions_infrastructure_availables')
      }
      listBoxVariable('version_provisioning') {
        required false
        showOnReleaseStart false
        label 'Versión de los playbooks de Ansible para provisionar la infraestructura'
        possibleValues '3.0.0', '2.0.0', '1.0.0'
        value '3.0.0'
      }
      stringVariable('ip_front') {
        required false
        showOnReleaseStart false
        label 'IP de la máquina de front'
        description 'IP obtenida de forma dinámica de las instancias EC2 creadas'
      }
      stringVariable('ip_bdd') {
        required false
        showOnReleaseStart false
        label 'IP de la máquina de bdd'
        description 'IP obtenida de forma dinámica de las instancias EC2 creadas'
      }
    }
    description '# Template para el provisionamiento de nueva infraestructura en AWS\n' +
                '\n' +
                'Esta template se encargar del provisionamiento de una nueva infraestructura en AWS'
    scheduledStartDate Date.parse("yyyy-MM-dd'T'HH:mm:ssZ", '2019-08-08T09:00:00+0200')
    phases {
      phase('OBTENCIÓN DE DATOS') {
        color '#0099CC'
        tasks {
          userInput('Parámetros de configuración e infraestructura') {
            description '## Datos para la creación de la infraestructura\n' +
                        'Es necesario proporcionar una serie de datos para poder realizar el provisioning de la infraestructura en AWS. **Será necesario facilitar los siguientes datos**.'
            team 'Desarrollo'
            variables {
              variable 'aws_region'
              variable 'environment'
              variable 'instance_type'
              variable 'project_name'
              variable 'public_key_path'
              variable 'private_key_path'
            }
          }
          custom('Verificación de existencia de proyecto de infraestructura para ${project_name}') {
            description '## Verificación de existencia de proyecto de infraestructura para ${project_name}\n' +
                        '\n' +
                        'Será el proyecto con las templates de Terraform que se encargará de hacer el provisioning  de la infraestructura.'
            team 'Desarrollo'
            script {
              type 'xld.DoesCIExist'
              server 'XL Deploy'
              ciID 'Applications/Infrastructures/infrastructure-${project_name}'
              throwOnFail true
              exists false
            }
          }
          custom('Obtención de las versiones disponibles para ${project_name}') {
            description '## Obtención de las versiones disponibles para ${project_name}\n' +
                        '\n' +
                        'Obtendremos un listado con las **distintas versiones de infraestructura disponibles** para el proyecto ${project_name}'
            team 'Desarrollo'
            script {
              type 'xld.GetAllVersions'
              server 'XL Deploy'
              applicationId 'Applications/Infrastructures/infrastructure-${project_name}'
              packageIds variable('versions_infrastructure_availables')
            }
          }
          userInput('Selección de versión de infraestructura') {
            description '## Selección de versión de la infraestructura\n' +
                        'Es necesario proporcionar la versión de la infraestructura que se desea aplicar. Una vez seleccionada, se tendrán:\n' +
                        '\n' +
                        '* Recursos creados en la región **${aws_region}**\n' +
                        '* Instancias del tipo **${instance_type}**\n' +
                        '* Entorno de tipo **${environment}**\n' +
                        '* Para el proyecto **${project_name}**'
            team 'Operaciones'
            variables {
              variable 'version_infrastructure_selected'
            }
          }
        }
      }
      phase('CREACIÓN DE ENTORNO') {
        color '#D94C3D'
        tasks {
          custom('Creación de infraestructura base en XLD') {
            description '## Creación de recursos en XLD\n' +
                        '\n' +
                        'Se invocará a la ejecución del script remoto `https://raw.githubusercontent.com/jclopeza/xlr-scripts/master/createXLDResourcesTerraformModuleJavaBddProject.py`\n' +
                        '\n' +
                        'Este script se ejecutará con el CLI para la creación de los recursos necesarios en XLD'
            script {
              type 'xld.cliUrl'
              cli 'xl-deploy-8.6.2-cli'
              scriptUrl 'https://raw.githubusercontent.com/jclopeza/xlr-scripts/master/createXLDResourcesTerraformModuleJavaBddProject.py'
              options '${environment} ${project_name} ${aws_region} ${instance_type} ${private_key_path} ${public_key_path}'
            }
          }
          manual('Validación de entorno en XLD') {
            description '### Recursos necesarios listos para despliegue de infraestructura\n' +
                        'Se han facilitado los siguientes datos para la creación de una nueva infraestructura:\n' +
                        '\n' +
                        '1. **Environment:** ${environment}\n' +
                        '2. **Proyecto:** ${project_name}\n' +
                        '3. **Región AWS:** ${aws_region}\n' +
                        '4. **Tipo de instancias:** ${instance_type}\n' +
                        '5. **Claves pública y privada:** ${public_key_path} y ${private_key_path}\n' +
                        '\n' +
                        '### Entorno de despliegue\n' +
                        'Se han creado en XL Deploy el entorno necesario para hacer el despliegue.\n' +
                        '\n' +
                        '#### Nombre del entorno de despliegue:\n' +
                        'Environments/infrastructure-${project_name}/infrastructure-${project_name}-${environment}/infrastructure-${project_name}-${environment}\n' +
                        '\n' +
                        '### Valide que se han creado correctamente los recursos en XLD'
            team 'Desarrollo'
          }
          gate('Validación de la solicitud de creación de infraestructura') {
            description '### Validación de solicitud\n' +
                        'El equipo de desarrollo solicita la creación de la siguiente infraestructura:\n' +
                        '\n' +
                        '1. **Environment:** ${environment}\n' +
                        '2. **Proyecto:** ${project_name}\n' +
                        '3. **Región AWS:** ${aws_region}\n' +
                        '4. **Tipo de instancias:** ${instance_type}\n' +
                        '5. **Claves pública y privada:** ${public_key_path} y ${private_key_path}\n' +
                        '\n' +
                        '### El entorno de despliegue ya ha sido validado\n' +
                        'Environments/infrastructure-${project_name}/infrastructure-${project_name}-${environment}/infrastructure-${project_name}-${environment}'
            team 'Operaciones'
            locked true
          }
          gate('Aprobación del equipo de seguridad') {
            description '### Validación de solicitud\n' +
                        'El equipo de desarrollo solicita la creación de la siguiente infraestructura:\n' +
                        '\n' +
                        '1. **Environment:** ${environment}\n' +
                        '2. **Proyecto:** ${project_name}\n' +
                        '3. **Región AWS:** ${aws_region}\n' +
                        '4. **Tipo de instancias:** ${instance_type}\n' +
                        '5. **Claves pública y privada:** ${public_key_path} y ${private_key_path}\n' +
                        '\n' +
                        '### El entorno de despliegue ya ha sido validado\n' +
                        'Environments/infrastructure-${project_name}/infrastructure-${project_name}-${environment}/infrastructure-${project_name}-${environment}'
            team 'Seguridad'
            precondition '"${environment}" == "pro"'
            locked true
          }
        }
      }
      phase('CREACIÓN DE INFRAESTRUCTURA') {
        color '#08B153'
        tasks {
          custom('Creación de infraestructura en AWS') {
            team 'Operaciones'
            script {
              type 'xldeploy.Deploy'
              server 'XL Deploy'
              retryCounter 'currentContinueRetrial':'0','currentPollingTrial':'0'
              deploymentPackage '${version_infrastructure_selected}'
              deploymentEnvironment 'Environments/infrastructure-${project_name}/infrastructure-${project_name}-${environment}/infrastructure-${project_name}-${environment}'
            }
          }
          notification('Infraestructura creada en AWS') {
            description '### Notificación de infraestructura creada\n' +
                        'Notificación al equipo de operaciones de que se ha creado la estructura y de que debe acometer ciertas operaciones.'
            team 'Operaciones'
            addresses 'ramon@gmail.com'
            subject 'Infraestructura provisionada para el proyecto ${project_name} y entorno ${environment}'
            body '### Infraestructura provisionada para el proyecto ${project_name} y entorno ${environment}\n' +
                 '\n' +
                 'Se ha creado nueva infraestructura en AWS. Recuerde que:\n' +
                 '\n' +
                 '* Debe habilitar la monitorización\n' +
                 '* Debe establecer alertas de consumo\n' +
                 '* Debe notificar cualquier anomalía o corte de servicio a la dirección sistemas@gmail.com\n' +
                 '\n' +
                 'Estos son los datos relacionados con la infraestructura:\n' +
                 '\n' +
                 '1. **Environment:** ${environment}\n' +
                 '2. **Proyecto:** ${project_name}\n' +
                 '3. **Región AWS:** ${aws_region}\n' +
                 '4. **Tipo de instancias:** ${instance_type}\n' +
                 '5. **Claves pública y privada:** ${public_key_path} y ${private_key_path}'
          }
          manual('Validación de conexión a hosts remotos') {
            description '### Validación de conexión a hosts remotos\n' +
                        'Se han creado nuevas instancias EC2 en XL Deploy. Debe acceder para comprobar que hay conectividad con las claves proporcionadas.\n' +
                        '\n' +
                        '#### Los nombres de las máquinas son "${project_name}-${environment}-*"'
            team 'Operaciones'
          }
          parallelGroup('Obtención de las IPs remotas') {
            tasks {
              custom('IP de la máquina front') {
                team 'Operaciones'
                script {
                  type 'xld.GetCIStringProperty'
                  server 'XL Deploy'
                  ciID 'Infrastructure/${project_name}-${environment}-front'
                  ciPropertyName 'address'
                  ciPropertyValue variable('ip_front')
                }
              }
              custom('IP de la máquina bdd') {
                team 'Operaciones'
                script {
                  type 'xld.GetCIStringProperty'
                  server 'XL Deploy'
                  ciID 'Infrastructure/${project_name}-${environment}-bdd'
                  ciPropertyName 'address'
                  ciPropertyValue variable('ip_bdd')
                }
              }
            }
          }
        }
      }
      phase('PROVISIONING') {
        color '#0099CC'
        tasks {
          userInput('Selección de versión de provisioning') {
            description '### Máquinas a provisionar\n' +
                        'Se van a provisionar las máquinas:\n' +
                        '\n' +
                        '#### ${project_name}-${environment}-front con IP = ${ip_front}\n' +
                        '#### ${project_name}-${environment}-bdd con IP = ${ip_bdd}\n' +
                        '\n' +
                        '### Versión de los playbooks\n' +
                        'Debe seleccionar la versión de los playbooks que se van a utilizar para hacer el provisioning del nuevo entorno creado.\n' +
                        '\n' +
                        '#### Debe seleccionar entre una de las versiones que hay disponibles para el proyecto ${project_name}'
            team 'Operaciones'
            variables {
              variable 'version_provisioning'
            }
          }
          parallelGroup('Provisioning de instancias') {
            tasks {
              custom('Provisioning de la instancia-front EC2 en Amazon') {
                description '### Ejecución del script de provisioning\n' +
                            'Este script provisionará la instancia EC2 que encargará de la parte front.'
                script {
                  type 'ansible.RunPlaybook'
                  host 'ansible-machine-control'
                  playbook 
                  playbookPath '/home/jcla/Projects/desarrollo/playbooks-provisioning/playbook-front.yml'
                  cmdParams '-u ubuntu -i "${ip_front}," --private-key "${private_key_path}" --ssh-common-args="-o StrictHostKeyChecking=no"'
                }
              }
              custom('Provisioning de la instancia-bdd EC2 en Amazon') {
                description '### Ejecución del script de provisioning\n' +
                            'Este script provisionará la instancia EC2 que encargará de la parte bdd.'
                script {
                  type 'ansible.RunPlaybook'
                  host 'ansible-machine-control'
                  playbook 
                  playbookPath '/home/jcla/Projects/desarrollo/playbooks-provisioning/playbook-bdd.yml'
                  cmdParams '-u ubuntu -i "${ip_bdd}," --private-key "${private_key_path}" --ssh-common-args="-o StrictHostKeyChecking=no"'
                }
              }
            }
          }
          custom('Creación de entorno en XLD') {
            description '## Creación de recursos en XLD\n' +
                        '\n' +
                        'Se invocará a la ejecución del script remoto `https://raw.githubusercontent.com/jclopeza/xlr-scripts/master/createXLDResourcesTerraformModuleJavaBddProjectContainers.py`\n' +
                        '\n' +
                        'Este script se ejecutará con el CLI para la creación de nuevos containers en XLD y un nuevo entorno de despliegue.'
            team 'Operaciones'
            script {
              type 'xld.cliUrl'
              cli 'xl-deploy-8.6.2-cli'
              scriptUrl 'https://raw.githubusercontent.com/jclopeza/xlr-scripts/master/createXLDResourcesTerraformModuleJavaBddProjectContainers.py'
              options '${environment} ${project_name}'
            }
          }
          notification('Notificación a desarrollo de nuevo entorno disponible') {
            description '### Notificación a desarrollo de nuevo entorno disponible\n' +
                        'Notificación al equipo de desarrollo de que se ha creado el nuevo entorno para que puedan desplegarse las aplicaciones correspondientes.'
            team 'Desarrollo'
            addresses 'oscar@gmail.com'
            subject 'Entorno disponible para ${project_name} y entorno ${environment}'
            body '### Entorno creado para el proyecto ${project_name} y entorno ${environment}\n' +
                 '\n' +
                 'Se ha creado un nuevo entorno en XL Deploy para que pueda desplegar su aplicación.'
          }
        }
      }
    }
    
  }
}