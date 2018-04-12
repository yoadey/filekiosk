import React, { Component } from 'react';
import './App.css';

class App extends Component {

  constructor(props) {
    super(props);
    this.state ={
        imagesInfos: [ { timeout: 200, id:0 }],
        currentImage: '0'
    };
  }

  tick() {
    this.fetchImageInfos();
    this.updateImage();
  }

  componentDidMount() {
    this.tick();
    var date = new Date();
    setTimeout(() => {
      this.interval = setInterval(() => this.tick(), 1000);
    }, 1000-date.getMilliseconds());
  }
  componentWillUnmount() {
    clearInterval(this.interval);
  }

  fetchImageInfos() {
    fetch('/imageinfos', {
      method: 'GET',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
      }
    })
    .then((response) => response.json())
    .then((responseJson) => {
      this.setState({
        imagesInfos: responseJson
      })
    });
  }

  updateImage() {
    var date = new Date();
    date.setMinutes(0);
    date.setSeconds(0);
    date.setMilliseconds(0);
    var msPassed = new Date().getTime() - date.getTime();
    // set to half a second so it is clear, to which second it belongs
    var msCalc = 500;
    var image = 0;
    var imageNumber = -1;
    var imagesInfos = this.state.imagesInfos;
    if(imagesInfos.length > 0) {
      while(msCalc < msPassed) {
        imageNumber++;
        imageNumber %= imagesInfos.length;
        var info = imagesInfos[imageNumber];
        msCalc += info.timeout;
      }
      if(this.state.nextImagePlain !== imagesInfos[imageNumber].id) {
        this.setState({
          currentImage: this.state.nextImage,
          nextImage: imagesInfos[imageNumber].id + "?" + new Date().getTime(),
          nextImagePlain: imagesInfos[imageNumber].id
        });
      }
    }
  }

  render() {
    return (
      <div className="App">
        // next image is already loaded to avoid slow rendering issues
        <div className="App" style={{'background-image': 'url(images/' + this.state.nextImage + ')'}}/>
        //  current image to show is in front and hides the other
        <div className="App" style={{'background-image': 'url(images/' + this.state.currentImage + ')'}}/>
      </div>
    );
  }
}

export default App;
