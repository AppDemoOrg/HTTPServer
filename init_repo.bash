#! /bin/bash
# git bash START
echo 'git init'
git init
echo 'git add -A'
git add -A
echo 'git commit -m "init repo"'
git commit -m "init repo"
echo 'git remote add origin https://github.com/AppDemoOrg/HTTPServer.git'
git remote add origin https://github.com/AppDemoOrg/HTTPServer.git
echo 'git push -u origin master'
git push -u origin master
# git bash DONE
echo 'git bash DONE.'