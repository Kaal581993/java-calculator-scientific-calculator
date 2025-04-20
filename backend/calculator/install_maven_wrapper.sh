#!/bin/bash

# Install Apache Maven on Fedora and set up mvnw (Maven Wrapper)

# Step 1: Install Maven using DNF
echo "Installing Maven..."
sudo dnf install -y maven

# Step 2: Verify installation
echo "Verifying Maven installation..."
mvn -version || {
  echo "Maven installation failed."
  exit 1
}

# Step 3: Ask for project directory (or create a new one)
read -p "Enter project directory name (will be created if it doesn't exist): " project_dir
mkdir -p "$project_dir"
cd "$project_dir" || exit 1

# Step 4: Generate a sample Maven project (if not already)
if [ ! -f "pom.xml" ]; then
  echo "Creating a sample Maven project..."
  mvn archetype:generate -DgroupId=com.example -DartifactId=myapp \
    -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
  cd myapp || exit 1
fi

# Step 5: Download and set up Maven Wrapper
echo "Setting up Maven Wrapper (mvnw)..."
mvn -N io.takari:maven:wrapper

# Step 6: Make wrapper scripts executable
chmod +x mvnw mvnw.cmd

# Step 7: Confirm wrapper setup
if [ -f "./mvnw" ]; then
  echo "Maven Wrapper has been successfully created!"
  echo "You can now run your project using './mvnw clean install'"
else
  echo "Failed to set up Maven Wrapper."
fi
