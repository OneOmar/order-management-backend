# Kubernetes Deployment

## Deploy
The Jenkins job `order-management-k8s-pipeline` builds and publishes
a versioned image to GHCR, updates the `order-app` Deployment,
and verifies application health.

## Check status
kubectl get deployments,pods,services -n order-management

## Check deployed image
kubectl get deployment order-app -n order-management \
  -o jsonpath='{.spec.template.spec.containers[0].image}{"\n"}'

## Rollback
Review available revisions:
kubectl rollout history deployment/order-app -n order-management

Inspect a revision before rolling back:
kubectl rollout history deployment/order-app -n order-management --revision=15

Rollback to the previous revision:
kubectl rollout undo deployment/order-app -n order-management

Rollback to a specific revision:
kubectl rollout undo deployment/order-app -n order-management --to-revision=15

Verify the rollback:
kubectl rollout status deployment/order-app -n order-management
kubectl get deployment order-app -n order-management \
  -o jsonpath='{.spec.template.spec.containers[0].image}{"\n"}'

## Important
- These commands target the `order-management` Kubernetes namespace.
- Review the target revision before executing a rollback.
- A rollback restores the Deployment configuration, not database data.
