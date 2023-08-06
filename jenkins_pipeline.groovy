pipeline {
    agent any
    
    stages {
        stage('Build') {
            steps {
                echo "Build started on ${currentBuild.startTime}"
                echo "Compiling the Python code..."
                sh 'python -m pip install Flask'
                sh 'python test_app.py'
                echo "Build completed on ${currentBuild.startTime}"
            }
        }
        
        stage('Deploy') {
            steps {
                echo "Deploying the Flask web application..."
                
                // Application Installation and Startup
                sh 'mkdir -p /web'
                sh 'curl -O https://bootstrap.pypa.io/get-pip.py'
                sh 'python get-pip.py --user'
                sh 'python -m pip install Flask'
                
                // Start Flask Application in the Background
                sh 'nohup python web.py > /dev/null 2>&1 &'
                
                echo "Flask web application has been deployed."
                
                // Wait for the application to start
                sleep 5
                
                // Send a POST request to /shutdown to stop the Flask application gracefully
                sh """
                    python - <<EOF
                    import requests
                    requests.post("http://127.0.0.1/shutdown")
                    EOF
                """
                
                echo "Flask web application has been stopped."
            }
        }
    }
}
