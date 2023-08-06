pipeline {
    agent any
    
    stages {
        stage('Build') {
            steps {
                timestamps {
                    echo "Build started"
                    echo "Compiling the python3 code..."
                    sh 'python3 -m pip install Flask'
                    sh 'python3 test_app.py'
                    echo "Build completed"
                }
            }
        }
        
        stage('Deploy') {
            steps {
                timestamps {
                    echo "Deploying the Flask web application..."
                    
                    // Application Installation and Startup
                    sh 'mkdir -p /web'
                    sh 'curl -O https://bootstrap.pypa.io/get-pip.py'
                    sh 'python3 get-pip.py --user'
                    sh 'python3 -m pip install Flask'
                    
                    // Start Flask Application in the Background
                    sh 'nohup python3 web.py > /dev/null 2>&1 &'
                    
                    echo "Flask web application has been deployed."
                    
                    // Wait for the application to start
                    sleep 5
                    
                    // Send a POST request to /shutdown to stop the Flask application gracefully
                    sh """
                        python3 - <<EOF
                        import requests
                        requests.post("http://127.0.0.1/shutdown")
                        EOF
                    """
                    
                    echo "Flask web application has been stopped."
                }
            }
        }
    }
}
