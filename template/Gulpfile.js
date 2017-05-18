var gulp = require('gulp'),
  less = require('gulp-less'),
  autoprefixer = require('gulp-autoprefixer'),
  concat = require('gulp-concat'),
  rename = require('gulp-rename'),
  clean = require('gulp-clean'),
  fileinclude = require('gulp-file-include'),
  runSequence = require('run-sequence'),
  browserSync = require('browser-sync'),
  reload = browserSync.reload;
path = require('path');
del = require('del');


gulp.task('fileinclude', function () {
  gulp.src(['index.html'])
    .pipe(fileinclude({
      prefix: '@@',
      basepath: '@file'
    }))
    .pipe(gulp.dest('./docs'));
});


gulp.task('less:main', function () {
  return gulp.src('./less/main.less')
    .pipe(less({
      paths: [path.join(__dirname, 'less', 'includes')]
    })).pipe(autoprefixer({
      browsers: ['last 2 versions'],
      cascade: false
    }))
    .pipe(gulp.dest('./docs/css'))
    .pipe(browserSync.stream());
});


gulp.task('copy:js', function () {
  return gulp.src([
    'node_modules/jquery/dist/jquery.js',
    'node_modules/bootstrap/dist/js/bootstrap.js'
  ])
    .pipe(concat('main.js'))
    .pipe(gulp.dest('./docs/js'));
});

gulp.task('browser-sync', function () {
  browserSync.init({
    injectChanges: true,
    server: {
      baseDir: "./docs"
    }
  });
  gulp.watch("less/**/*.*", ['less']);
  gulp.watch("_includes/**/*.*", ['fileinclude']);
  gulp.watch("docs/**/*.*").on("change", reload);

});

gulp.task('less', function (callback) {
  runSequence(['less:main'],
    callback
  )
});

gulp.task('default', function (callback) {
  runSequence(['copy:js', 'less', 'fileinclude'], 'browser-sync',
    callback
  )
});
