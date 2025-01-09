export default class OllamaRequest {

    model: string;
    prompt: string;
    format: string = "";
    stream: boolean = false;


    constructor(model: string, prompt: string) {
        this.model = model;
        this.prompt = prompt;
    }
}