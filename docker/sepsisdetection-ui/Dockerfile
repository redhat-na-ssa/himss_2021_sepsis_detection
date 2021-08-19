#  Build:  buildah bud -f docker/sepsisdetection-ui/Dockerfile -t quay.io/redhat_naps_da/sepsisdetection-ui:0.0.1 sepsisdetection-ui
#  Run  :  podman run -it -p 7080:8080 --rm quay.io/redhat_naps_da/sepsisdetection-ui:0.0.1


# Stage 1: Compile and Build angular codebase

# Use official node image as the base image
FROM registry.access.redhat.com/ubi8/nodejs-14:1-43 as build

# Add the source code to app
COPY ./ .

# Install all the dependencies
RUN npm install

# Generate the build of the application (in: /opt/app-root/src )
RUN npm run build



# Stage 2: Serve app with nginx server

# Use official nginx image as the base image
FROM registry.access.redhat.com/ubi8/nginx-118:1-39

# Copy the build output to replace the default nginx contents.
COPY --from=build /opt/app-root/src/dist/sepsis-ui .

# Expose port
EXPOSE 8080
CMD ["nginx", "-g", "daemon off;"]